package net.metisapp.metisapi.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sentry.Sentry;
import net.metisapp.metisapi.entities.DisabledRule;
import net.metisapp.metisapi.entities.MetisUser;
import net.metisapp.metisapi.repositories.DisabledRuleRepository;
import net.metisapp.metisapi.repositories.UserRepository;
import net.metisapp.metisapi.responses.JSONResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class Disabled {

	@Autowired
	private UserRepository userRepository;

	private final Logger log = LoggerFactory.getLogger(Disabled.class);

	@Autowired
	private DisabledRuleRepository disabledRuleRepository;

	private boolean checkRole(Collection<? extends GrantedAuthority> authorities, String role){
		return authorities.stream().anyMatch(auth -> auth.getAuthority().equals(role));
	}

	@GetMapping("/disabled/rules")
	public List<DisabledRule> getRules(Authentication auth){
		return userRepository.findByEmail(auth.getName()).getDisabled();
	}

	@GetMapping("/disabled") // Return a list of appointments
	public HashMap<String, List<Calendar[]>> getDisabledRules(@RequestParam(value = "start", defaultValue = "today") String startStr, @RequestParam(value = "end", defaultValue = "today") String endStr, Authentication auth) {
		Calendar today = new GregorianCalendar();
		Calendar start = new GregorianCalendar(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DATE), 0, 0, 0); // Set default times in case the user didn't send any input
		Calendar end = new GregorianCalendar(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DATE), 23, 59, 59);

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

		try{
			if( !startStr.equals("today") ){
				start.setTime(format.parse(startStr));
			}
			if( !endStr.equals("today") ){
				end.setTime(format.parse(endStr));
			}
		}catch( ParseException ex ){
			Sentry.captureException(ex);
		}

		HashMap<String, List<Calendar[]>> intervals= new HashMap<String, List<Calendar[]>>();

		if(start.after(end) || start.equals(end)) return intervals;
		int count = 0;
		MetisUser doctor;

		if(this.checkRole(auth.getAuthorities(), "PATIENT")){
			doctor = userRepository.findByEmail(auth.getName()).getDoctor();
		}else{
			doctor = userRepository.findByEmail(auth.getName());
		}

		userRepository.findByEmail(auth.getName());
		for(DisabledRule r : doctor.getDisabled()){
			if(r.getStart().after(end)) continue;

			long offset = 0;
			long diff = end.getTimeInMillis() - start.getTimeInMillis();
			if(!start.before(r.getStart())){
				offset = (start.getTimeInMillis() - r.getStart().getTimeInMillis()) % (r.getDuration() + r.getRepetition());
			}

			Calendar begin = Calendar.getInstance();
			Calendar stop = Calendar.getInstance();

			if(r.getStart().after(start)){
				begin = (Calendar) r.getStart().clone();
			}else if(offset < r.getDuration()){
				begin = (Calendar) start.clone();
			}else{
				if((r.getDuration() + r.getRepetition()) - offset < diff){
					begin.setTimeInMillis(start.getTimeInMillis() + (r.getDuration() + r.getRepetition()) - offset);
				}else{
					continue;
				}
			}

			List<Calendar[]> pairs = new LinkedList<Calendar[]>();

			do{
				if(count > 255){
					intervals.clear();
					return intervals;
				}
				Calendar[] calArr = new GregorianCalendar[2];
				calArr[0] = (Calendar) begin.clone();
				stop.setTimeInMillis(begin.getTimeInMillis() + (r.getDuration()));
				calArr[1] = end.getTimeInMillis() - stop.getTimeInMillis() < 0 ? (Calendar) end.clone() : (Calendar) stop.clone();
				begin.setTimeInMillis(stop.getTimeInMillis() + r.getRepetition());
				pairs.add(calArr);
				count++;
			}while(begin.before(end));
			intervals.put(r.getName(), pairs);
		}
		return intervals;
	}

	@PostMapping(path="/disabled")
	public JSONResponse postDisabledRule(@RequestBody String body, Authentication auth){
		// TODO Implement CSRF Protection
		DisabledRule rule;
		try{ // Parse the POST request body into an Appointment object.
			rule = new ObjectMapper().readValue(body, DisabledRule.class);
		}catch(Exception ex){
			return new JSONResponse(500, "Server could not parse that. This could be because you submitted invalid data.");
		}
		log.info(body);

		rule.setDoctor(userRepository.findByEmail(auth.getName()));
		disabledRuleRepository.save(rule);
		return new JSONResponse(200, "Succesfully added rule.");
	}

	@PostMapping(path="/disabled/update")
	@Transactional(propagation = Propagation.REQUIRED)
	public JSONResponse postDisabledRuleUpdate(@RequestBody String body, Authentication auth){

		if(checkRole(auth.getAuthorities(), "PATIENT")){
			return new JSONResponse(403, "You are not authorized to do this!");
		}
		// TODO Implement CSRF Protection
		DisabledRule rule;
		try{ // Parse the POST request body into an Appointment object.
			rule = new ObjectMapper().readValue(body, DisabledRule.class);
		}catch(Exception ex){
			return new JSONResponse(500, "Server could not parse that. This could be because you submitted invalid data.");
		}

		if(disabledRuleRepository.checkDoctor(rule.getId(), userRepository.findByEmail(auth.getName())).size() < 1){
			return new JSONResponse(500, "You don't have permission to access this rule!");
		}

		try{
			int edited = this.disabledRuleRepository.updateRule(rule.getName(), rule.getStart(), rule.getRepetition(), rule.getDuration(), rule.getId());
		}catch(DataIntegrityViolationException ex){
			return new JSONResponse(500, "Your input has invalid data!");
		}
		return new JSONResponse(200, "Success");
	}

	@GetMapping(path="/disabled/delete")
	@Transactional(propagation = Propagation.REQUIRED)
	public JSONResponse postDisabledRuleDelete(@RequestParam(value = "id", defaultValue = "-1") String idStr, Authentication auth){
		long id = Long.parseLong(idStr);
		if(id == -1) return new JSONResponse(500, "You must submit an id");
		int deleted = disabledRuleRepository.deleteRule(id, userRepository.findByEmail(auth.getName()));

		if(deleted != 1){
			return new JSONResponse(500, "This rule either does not exist or does not belong to you.");
		}
		return new JSONResponse(200, "Success");
	}

}
