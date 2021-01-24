package net.metisapp.metisapi.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sentry.Sentry;
import net.metisapp.metisapi.entities.Appointment;
import net.metisapp.metisapi.entities.MetisUser;
import net.metisapp.metisapi.repositories.AppointmentRepository;
import net.metisapp.metisapi.repositories.UserRepository;
import net.metisapp.metisapi.responses.JSONResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

@RestController
public class Appointments {
	@Autowired
	private AppointmentRepository appointmentRepository;

	@Autowired
	private UserRepository userRepository;

	private final int MAX_APPOINTMENT_RESPONSE_SIZE = 1000;

	private final Logger log = LoggerFactory.getLogger(Appointments.class);

	private boolean checkOverlap(MetisUser doc, Calendar start, Calendar end){
		Calendar day_start = (Calendar) start.clone();
		Calendar day_end = (Calendar) end.clone();
		day_end.set(Calendar.HOUR_OF_DAY, 0);
		day_end.set(Calendar.MINUTE, 0);
		day_end.set(Calendar.SECOND, 0);
		day_start.set(Calendar.HOUR_OF_DAY, 0);
		day_start.set(Calendar.MINUTE, 0);
		day_start.set(Calendar.SECOND, 0);

		List<Appointment> apps = appointmentRepository.findAllBetweenDoctor(day_start, day_end, doc);

		for(Appointment a : apps){
			if(a.dateInAppointment(start) || a.dateInAppointment(end)) return false;
		}

		return true;
	}

	@GetMapping("/appointments") // Return a list of appointments
	public List<Appointment> getAppointments(@RequestParam(value = "start", defaultValue = "today") String startStr, @RequestParam(value = "end", defaultValue = "today") String endStr, Authentication auth) {
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

		MetisUser doctor = userRepository.findByEmail(auth.getName());
		List<Appointment> appointments = appointmentRepository.findAllBetweenDoctor(start, end, doctor);

		if(start.after(end) || start.equals(end)) return appointments;

		if( appointments.size() > MAX_APPOINTMENT_RESPONSE_SIZE ){ // Do not return a response in case the range is too big. Prevents DOS.
			appointments.clear();
		}

		return appointments;
	}

	@PostMapping(path="/appointments")
	public JSONResponse postAppointment(@RequestBody String body, Authentication auth){
		Appointment appointment;
		try{ // Parse the POST request body into an Appointment object.
			appointment = new ObjectMapper().readValue(body, Appointment.class);
		}catch(Exception ex){
			return new JSONResponse(500, "Server could not parse that. This could be because you submitted invalid data.");
		}
		log.info(body);

		appointment.setDoctor(userRepository.findByEmail(auth.getName()));
		if(!checkOverlap(userRepository.findByEmail(auth.getName()), appointment.getStart(), appointment.getEnd())){
			return new JSONResponse(400, "Dates you have submitted overlaps with already existing ones.");
		}

		if(userRepository.checkDoctor(appointment.getPatient().getEmail(), appointment.getDoctor()).size()  < 1){
			return new JSONResponse(500, "This patient either does not exist or is not your patient.");
		}

		if(appointment.checkDisabled()){
			return new JSONResponse(500, "This time is disabled by the doctor.");
		}
		return new JSONResponse(200, "Appointment created succcesfully", appointmentRepository.save(appointment));
	}

	@PostMapping(path="/appointments/delete")
	@Transactional(propagation = Propagation.REQUIRED)
	public JSONResponse postAppointmentDelete(@RequestBody String body, Authentication auth){
		// TODO Implement CSRF Protection
		// TODO more code to delete patients who have appointments
		Appointment appointment;
		try{ // Parse the POST request body into an Appointment object.
			appointment = new ObjectMapper().readValue(body, Appointment.class);
		}catch(Exception ex){
			return new JSONResponse(500, "Server could not parse that. This could be because you submitted invalid data.");
		}
		try{
			this.appointmentRepository.deleteAppointment(appointment.getId(), userRepository.findByEmail(auth.getName()));
		}catch(DataIntegrityViolationException ex){
			return new JSONResponse(500, "Can't delete that!");
		}
		return new JSONResponse(200, "Success");
	}

}
