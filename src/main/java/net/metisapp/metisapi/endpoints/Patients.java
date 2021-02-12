package net.metisapp.metisapi.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sentry.Sentry;
import net.metisapp.metisapi.entities.Appointment;
import net.metisapp.metisapi.entities.MetisUser;
import net.metisapp.metisapi.entities.ProtocolNumber;
import net.metisapp.metisapi.repositories.AppointmentRepository;
import net.metisapp.metisapi.repositories.UserRepository;
import net.metisapp.metisapi.responses.JSONResponse;
import net.metisapp.metisapi.responses.PatientsResponse;
import net.metisapp.metisapi.responses.StandardResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.metrics.annotation.Timed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Timed
public class Patients {

	@Autowired
	private AppointmentRepository appointmentRepository;

	@Autowired
	private UserRepository userRepository;

	private boolean checkRole(Collection<? extends GrantedAuthority> authorities, String role){
		return authorities.stream().anyMatch(auth -> auth.getAuthority().equals(role));
	}

	private String capitalizeFirstLetters(String phrase){
		String[] words = phrase.trim().toLowerCase().split(" ");
		String finalPhrase = "";
		for(int i = 0; i < words.length; i++){
			finalPhrase += words[i].substring(0, 1).toUpperCase() + words[i].substring(1) + " ";
		}
		return finalPhrase.trim();
	}

	private String removeChar(String str, char c) {
		String final_str = "";
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) != c) {
				final_str += str.charAt(i);
			}
		}
		return final_str;
	}

	private Map<String, String> bodyToMap(String bodyStr){
		Map<String, String> body = new HashMap<>();
		String[] values = bodyStr.split("&");
		for (String value : values) {
			String[] pair = value.split("=");
			if (pair.length == 2) {
				body.put(pair[0], pair[1]);
			}
		}
		return body;
	}

	@GetMapping("/patients")
	public StandardResponse getPatient(@RequestParam(value="email", defaultValue = "") String email, @RequestParam(value="phone", defaultValue = "") String phone, @RequestParam(value="name", defaultValue = "") String name, @RequestParam(value="page", defaultValue="0") int page, @RequestParam(value="psize", defaultValue="20") int psize, @RequestParam(value="all", defaultValue="no") String listAll, Authentication auth) {
		Pageable pageable = PageRequest.of(page < 0 ? 0 : page, psize);
		if(checkRole(auth.getAuthorities(), "PATIENT")){
			return new JSONResponse(403, "You are not authorized to do this!");
		}

		List<MetisUser> patients;
		long patient_num;
		if(listAll.equals("yes")){
			patients = userRepository.searchUser(email, phone, name.isEmpty() ? "" : capitalizeFirstLetters(name), "PATIENT", pageable);
			patient_num = userRepository.countPatients(email, phone, name.isEmpty() ? "" : capitalizeFirstLetters(name), "PATIENT");
		}else{
			patients = userRepository.searchUser(email, phone, name.isEmpty() ? "" : capitalizeFirstLetters(name), auth.getName(), "PATIENT", pageable);
			patient_num = userRepository.countPatients(email, phone, name.isEmpty() ? "" : capitalizeFirstLetters(name), auth.getName(), "PATIENT");
		}
		if(patients.size() > 1000){
			patients.clear();
		}
		return new PatientsResponse(patient_num, patients); // TODO Write a method in UserRepository to get the number of patients. As this definitely won't work.
	}


	@PostMapping(path="/patients/create")
	public JSONResponse postPatientCreate(@RequestBody String body, Authentication auth){
		if(checkRole(auth.getAuthorities(), "PATIENT")){
			return new JSONResponse(403, "You are not authorized to do this!");
		}

		// TODO Implement CSRF Protection
		MetisUser patient;
		try{ // Parse the POST request body into an Appointment object.
			patient = new ObjectMapper().readValue(body, MetisUser.class);
		}catch(Exception ex){
			return new JSONResponse(500, "Server could not parse that. This could be because you submitted invalid data.");
		}

		if(!patient.getRole().equals("PATIENT") && !checkRole(auth.getAuthorities(),"ADMIN")){
			return new JSONResponse(403, "You cannot create this role!");
		}
		if(patient.getEmail().isEmpty()){
			patient.setEmail(UUID.randomUUID().toString());
		}
		patient.setDoctor(userRepository.findByEmail(auth.getName()));
		patient.setName(capitalizeFirstLetters(patient.getName()));
		patient.setHESCode(this.removeChar(patient.getHESCode().toUpperCase().strip(), '-'));
		try{
			this.userRepository.save(patient);
		}catch(DataIntegrityViolationException ex){
			return new JSONResponse(500, "A user with that e-mail already exists!");
		}
		return new JSONResponse(200, "Success");
	}

	@PostMapping(path="/patient/password")
	@Transactional(propagation = Propagation.REQUIRED)
	public JSONResponse postDoctorPassword(@RequestBody String body, Authentication auth){
		Map<String, String> data = this.bodyToMap(body);

		if(!(data.containsKey("email") && data.containsKey("newPassword"))){
			return new JSONResponse(500, "Missing parameters!");
		}
		if(checkRole(auth.getAuthorities(), "PATIENT")){
			return new JSONResponse(401, "You cannot do that!");
		}
		if(!checkRole(auth.getAuthorities(), "ADMIN") && userRepository.checkDoctor(data.get("email"), userRepository.findByEmail(auth.getName())).size() != 1){
			return new JSONResponse(500, "This patient either does not exist or is not your patient.");
		}
		BCryptPasswordEncoder pwdEncoder = new BCryptPasswordEncoder();
		userRepository.updatePassword(data.get("email"), pwdEncoder.encode(data.get("newPassword")));
		return new JSONResponse(200, "Password changed succcesfully.");
	}

	@PostMapping(path="/patients/update")
	@Transactional(propagation = Propagation.REQUIRED)
	public JSONResponse postPatientUpdate(@RequestBody String body, Authentication auth){

		if(checkRole(auth.getAuthorities(), "PATIENT")){
			return new JSONResponse(403, "You are not authorized to do this!");
		}
		MetisUser patient;
		try{ // Parse the POST request body into an Appointment object.
			patient = new ObjectMapper().readValue(body, MetisUser.class);
		}catch(Exception ex){
			return new JSONResponse(500, "Server could not parse that. This could be because you submitted invalid data.");
		}
		if(checkRole(auth.getAuthorities(), "DOCTOR") && userRepository.checkDoctor(patient.getId(), userRepository.findByEmail(auth.getName())).size() != 1){
			return new JSONResponse(500, "This patient either does not exist or is not your patient.");
		}
		if(!patient.getRole().equals("PATIENT") && !checkRole(auth.getAuthorities(),"ADMIN")){
			return new JSONResponse(403, "You cannot create this role!");
		}
		patient.setName(capitalizeFirstLetters(patient.getName()));
		patient.setHESCode(this.removeChar(patient.getHESCode().toUpperCase().strip(), '-'));
		try{
			int edited = this.userRepository.updatePatient(patient.getName(), patient.getEmail(), patient.getPhone(), patient.getTCNo(), patient.getHESCode(), patient.getRole(), patient.getProtocolNumbers(), patient.getId());
		}catch(DataIntegrityViolationException ex){
			return new JSONResponse(500, "A user with that e-mail already exists!");
		}

		return new JSONResponse(200, "Success");
	}

	@PostMapping(path="/patients/delete")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public JSONResponse postPatientDelete(@RequestBody String body, Authentication auth){
		if(checkRole(auth.getAuthorities(), "PATIENT")){
			return new JSONResponse(403, "You are not authorized to do this!");
		}
		MetisUser patient;
		try{ // Parse the POST request body into an Appointment object.
			patient = new ObjectMapper().readValue(body, MetisUser.class);
		}catch(Exception ex){
			return new JSONResponse(500, "Server could not parse that. This could be because you submitted invalid data.");
		}
		if(!patient.getRole().equals("PATIENT") && !checkRole(auth.getAuthorities(),"ADMIN")){
			return new JSONResponse(403, "You cannot delete this role!");
		}
		if(this.userRepository.checkDoctor(patient.getId(), this.userRepository.findByEmail(auth.getName())).size() != 1){
			return new JSONResponse(403, "You cannot delete this role!");
		}

		patient.setName(capitalizeFirstLetters(patient.getName()));
		List<Appointment> appointments = this.appointmentRepository.findAllPatients(this.userRepository.findById(patient.getId()));
		try{
			this.appointmentRepository.deleteAll(appointments);
			this.userRepository.delete(this.userRepository.findById(patient.getId()));
		}catch(DataIntegrityViolationException ex){
			return new JSONResponse(500, "An unknown error occured."); //TODO Better error message
		}
		return new JSONResponse(200, "Success");
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@GetMapping(path="/profile/hes/check")
	public JSONResponse getHesCheck(@RequestParam(value = "id") long id, Authentication auth){
		MetisUser doctor = userRepository.findByEmail(auth.getName());
		if(userRepository.checkDoctor(id, doctor).size() != 1){
			return new JSONResponse(500, "This patient either does not exist or is not your patient.");
		}
		boolean result;
		try{
			result = userRepository.findById(id).checkHesCode();
		}catch(Exception e){
			Sentry.captureException(e);
			return new JSONResponse(500, "An issue occured");
		}
		userRepository.updateCovidStatus(id, result);
		return new JSONResponse(200, result ? "safe":"unsafe");
	}
}
