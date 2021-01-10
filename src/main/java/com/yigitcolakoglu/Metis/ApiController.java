package com.yigitcolakoglu.Metis;

import java.lang.Exception;
import io.sentry.Sentry;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.GrantedAuthority;

import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper; 
import com.fasterxml.jackson.databind.node.ObjectNode; 

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Arrays;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class ApiController {
        // TODO Session management 

        private final int MAX_APPOINTMENT_RESPONSE_SIZE = 1000;
        private final Logger log = LoggerFactory.getLogger(CheckHesCodeTask.class);

        @Autowired
        private AppointmentRepository appointmentRepository;

        @Autowired
        private UserRepository userRepository;
        
        @GetMapping("/api/appointments") // Return a list of appointments 
        public List<Appointment> getAppointments(@RequestParam(value = "start", defaultValue = "today") String startStr, @RequestParam(value = "end", defaultValue = "today") String endStr, Authentication auth) { 
            Date start = new Date(); // Set default times in case the user didn't send any input 
            start.setHours(0); // TODO this is deprecated, change
            start.setMinutes(0);
            start.setSeconds(0);

            Date end = new Date(); 
            end.setHours(23);
            end.setMinutes(59);
            end.setSeconds(59);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            try{
                if( !startStr.equals("today") ){
                    start = format.parse(startStr);
                }
                if( !endStr.equals("today") ){
                    end = format.parse(endStr);
                }
            }catch( ParseException ex ){
                Sentry.captureException(ex);
            }

            User doctor = userRepository.findByEmail(auth.getName());
            List<Appointment> appointments = appointmentRepository.findAllBetweenDoctor(start, end, doctor); 

            if( appointments.size() > MAX_APPOINTMENT_RESPONSE_SIZE ){ // Do not return a response in case the range is too big. Prevents DOS.
                appointments.clear();
            }

            return appointments;
        }
        
        @PostMapping(path="/api/appointments")
        public JSONResponse postAppointment(@RequestBody String body, Authentication auth){
            // TODO Implement CSRF Protection
            Appointment appointment;
            try{ // Parse the POST request body into an Appointment object.
                appointment = new ObjectMapper().readValue(body, Appointment.class);
            }catch(Exception ex){
                Sentry.captureException(ex);
                Sentry.captureMessage(body);
                return new JSONResponse(500, "Server could not parse that. This could be because you submitted invalid data.");
            }
            log.info(body);

            // TODO check if doctor ids match with cookie
            boolean overlapped = appointmentRepository.findOverlaps(appointment.getStart(), appointment.getEnd(), appointment.getDoctor()); // Check for overlaps in the submitted dates.
            appointment.setDoctor(userRepository.findByEmail(auth.getName()));

            if(overlapped){
                return new JSONResponse(400, "Dates you have submitted overlaps with already existing ones.");
            }

            if(userRepository.checkDoctor(appointment.getPatient().getEmail(), appointment.getDoctor()).size() != 1){
                return new JSONResponse(500, "This patient either does not exist or is not your patient.");
            } 
            
            if(appointment.checkDisabled()){
                return new JSONResponse(500, "This time is disabled by the doctor.");
            }
            return new JSONResponse(200, appointmentRepository.save(appointment));
        }

        @PostMapping(path="/api/appointments/delete")
        @Transactional(propagation = Propagation.REQUIRED)
        public JSONResponse postAppointmentDelete(@RequestBody String body, Authentication auth){
            // TODO Implement CSRF Protection
            // TODO more code to delete patients who have appointments
            Appointment appointment;
            try{ // Parse the POST request body into an Appointment object.
                appointment = new ObjectMapper().readValue(body, Appointment.class);
            }catch(Exception ex){
                Sentry.captureException(ex);
                Sentry.captureMessage(body);
                return new JSONResponse(500, "Server could not parse that. This could be because you submitted invalid data.");
            }  
            try{
                this.appointmentRepository.deleteAppointment(appointment.getId(), userRepository.findByEmail(auth.getName()));
            }catch(DataIntegrityViolationException ex){
                Sentry.captureException(ex);
                Sentry.captureMessage(body);
                return new JSONResponse(500, "Can't delete that!");
            }
            return new JSONResponse(200, "Success");
        } 

        private String capitalizeFirstLetters(String phrase){
            String[] words = phrase.trim().toLowerCase().split(" ");
            String finalPhrase = "";
            for(int i = 0; i < words.length; i++){
                finalPhrase += words[i].substring(0, 1).toUpperCase() + words[i].substring(1) + " ";
            }
            return finalPhrase.trim();
        }

        @GetMapping("/api/patients")
        public StandardResponse getPatient(@RequestParam(value="email") String email, @RequestParam(value="phone") String phone, @RequestParam(value="name") String name, @RequestParam(value="page", defaultValue="0") int page, @RequestParam(value="psize", defaultValue="20") int psize, @RequestParam(value="role") String role, @RequestParam(value="all", defaultValue="no") String listAll, Authentication auth) {
            Pageable pageable = PageRequest.of(page < 0 ? 0 : page, psize);
            if(checkRole(auth.getAuthorities(), "PATIENT")){
               return new JSONResponse(403, "You are not authorized to do this!"); 
            }

            if((!role.equals("PATIENT") || listAll.equals("yes")) && !checkRole(auth.getAuthorities(),"ADMIN")){
               return new JSONResponse(403, "You cannot list this role!"); 
            }

            List<User> patients;
            long patient_num;
            if(listAll.equals("yes")){
                patients = userRepository.searchUser(email, phone, name.isEmpty() ? "" : capitalizeFirstLetters(name), role, pageable); 
                patient_num = userRepository.countPatients(email, phone, name.isEmpty() ? "" : capitalizeFirstLetters(name), role);
            }else{
                User doctor = userRepository.findByEmail(auth.getName());
                patients = userRepository.searchUser(email, phone, name.isEmpty() ? "" : capitalizeFirstLetters(name), doctor, role, pageable); 
                patient_num = userRepository.countPatients(email, phone, name.isEmpty() ? "" : capitalizeFirstLetters(name), doctor, role);
            }
            if(patients.size() > 1000){
                patients.clear();
            }
            return new PatientsResponse(patient_num, patients); // TODO Write a method in UserRepository to get the number of patients. As this definitely won't work. 
        }

        private boolean checkRole(Collection<? extends GrantedAuthority> authorities, String role){
            return authorities.stream().anyMatch(auth -> auth.getAuthority().equals(role));
        }

        private String removeChar(String str, char c){
            String final_str = "";
            for(int i = 0; i < str.length(); i++){
                if(str.charAt(i) != c){
                    final_str += str.charAt(i);
                }
            }
            return final_str;
        }
        @PostMapping(path="/api/patients/create")
        public JSONResponse postPatientCreate(@RequestBody String body, Authentication auth){
            if(checkRole(auth.getAuthorities(), "PATIENT")){
               return new JSONResponse(403, "You are not authorized to do this!"); 
            }

            // TODO Implement CSRF Protection
            User patient;
            try{ // Parse the POST request body into an Appointment object.
                patient = new ObjectMapper().readValue(body, User.class);
            }catch(Exception ex){
                Sentry.captureException(ex);
                Sentry.captureMessage(body);
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
                Sentry.captureException(ex);
                Sentry.captureMessage(body);
                return new JSONResponse(500, "A user with that e-mail already exists!");
            }
            return new JSONResponse(200, "Success");
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

        @PostMapping(path="/api/patient/password")
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

        @PostMapping(path="/api/password")
        @Transactional(propagation = Propagation.REQUIRED)
        public JSONResponse postPassword(@RequestBody String body, Authentication auth){
            Map<String, String> data = this.bodyToMap(body); 

            if(!(data.containsKey("oldPassword") && data.containsKey("newPassword"))){
                return new JSONResponse(500, "Missing parameters!");
            }
            BCryptPasswordEncoder pwdEncoder = new BCryptPasswordEncoder();
            if(!pwdEncoder.matches(data.get("oldPassword"), userRepository.findByEmail(auth.getName()).getPassword())){
                return new JSONResponse(500, "Old password incorrect.");
            }
            userRepository.updatePassword(auth.getName(), pwdEncoder.encode(data.get("newPassword")));
            return new JSONResponse(200, "Password changed succcesfully.");
        } 
        
        @PostMapping(path="/api/patients/update")
        @Transactional(propagation = Propagation.REQUIRED)
        public JSONResponse postPatientUpdate(@RequestBody String body, Authentication auth){

            if(checkRole(auth.getAuthorities(), "PATIENT")){
               return new JSONResponse(403, "You are not authorized to do this!"); 
            }
            // TODO Implement CSRF Protection
            User patient;
            try{ // Parse the POST request body into an Appointment object.
                patient = new ObjectMapper().readValue(body, User.class);
            }catch(Exception ex){
                Sentry.captureException(ex);
                Sentry.captureMessage(body);
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
                int edited = this.userRepository.updatePatient(patient.getName(), patient.getEmail(), patient.getPhone(), patient.getTCNo(), patient.getHESCode(), patient.getRole(), patient.getId());
            }catch(DataIntegrityViolationException ex){
                Sentry.captureException(ex);
                Sentry.captureMessage(body);
                return new JSONResponse(500, "A user with that e-mail already exists!");
            }
            return new JSONResponse(200, "Success");
        } 

        @PostMapping(path="/api/patients/delete")
        @Transactional(propagation = Propagation.REQUIRED)
        public JSONResponse postPatientDelete(@RequestBody String body, Authentication auth){
            // TODO Implement CSRF Protection
            // TODO more code to delete patients who have appointments
            if(checkRole(auth.getAuthorities(), "PATIENT")){
               return new JSONResponse(403, "You are not authorized to do this!"); 
            }
            User patient;
            try{ // Parse the POST request body into an Appointment object.
                patient = new ObjectMapper().readValue(body, User.class);
            }catch(Exception ex){
                Sentry.captureException(ex);
                Sentry.captureMessage(body);
                return new JSONResponse(500, "Server could not parse that. This could be because you submitted invalid data.");
            }  
            if(!patient.getRole().equals("PATIENT") && !checkRole(auth.getAuthorities(),"ADMIN")){
               return new JSONResponse(403, "You cannot list this role!"); 
            }
            patient.setName(capitalizeFirstLetters(patient.getName()));
            try{
                this.userRepository.deletePatient(patient.getId(), userRepository.findByEmail(auth.getName()));
            }catch(DataIntegrityViolationException ex){
                Sentry.captureException(ex);
                Sentry.captureMessage(body);
                return new JSONResponse(500, "A user with that e-mail already exists!");
            }
            return new JSONResponse(200, "Success");
        } 

        @GetMapping(path="/api/profile")
        public User getProfile(Authentication auth){
            return userRepository.findByEmail(auth.getName());
        } 
        
        @PostMapping(path="/api/profile/update")
        @Transactional(propagation = Propagation.REQUIRED)
        public StandardResponse postProfileUpdate(@RequestBody String body, Authentication auth){
            User profile;
            try{ // Parse the POST request body into an Appointment object.
                profile = new ObjectMapper().readValue(body, User.class);
            }catch(Exception ex){
                Sentry.captureException(ex);
                Sentry.captureMessage(body);
                return new JSONResponse(500, "Server could not parse that. This could be because you submitted invalid data.");
            }
            profile.setName(capitalizeFirstLetters(profile.getName()));
            try{
                int edited = this.userRepository.updateUser(profile.getName(), profile.getEmail(), profile.getPhone(), profile.getTCNo(),profile.getHESCode(), profile.getLocale(), userRepository.findByEmail(auth.getName()).getId());
            }catch(DataIntegrityViolationException ex){
                System.out.println(ex.toString());
                return new JSONResponse(500, "A user with that e-mail already exists!");
            }
            return new JSONResponse(200, "Success");
        }

        @GetMapping(path="/api/hes/sendsms")
        public JSONResponse sendHesSMS(Authentication auth){ // TODO Error handling
            String phone = userRepository.findByEmail(auth.getName()).getPhone();
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode request_obj = mapper.createObjectNode();
            request_obj.put("phone",phone);
            HttpEntity<String> request = new HttpEntity<String>(request_obj.toString(), headers);
            ResponseEntity<String> result = restTemplate.postForEntity("https://hessvc.saglik.gov.tr/api/send-code-to-login", request, String.class); 
            return new JSONResponse(200, "Sent SMS");
        }

        @GetMapping(path="/api/hes/smscode")
        @Transactional(propagation = Propagation.REQUIRED)
        public JSONResponse sendHesSMS(@RequestParam(value = "code") String smsCode ,Authentication auth){ // TODO Error handling
            String phone = userRepository.findByEmail(auth.getName()).getPhone();
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode request_obj = mapper.createObjectNode();
            request_obj.put("phone",phone);
            request_obj.put("password",smsCode);
            request_obj.put("rememberMe",true);
            HttpEntity<String> request = new HttpEntity<String>(request_obj.toString(), headers);
            String token = "";
            try{
                ResponseEntity<String> result = restTemplate.postForEntity("https://hessvc.saglik.gov.tr/api/authenticate-with-code", request, String.class); 
                JSONObject response = new JSONObject(result.getBody());
                token = response.getString("id_token");
            }catch(Exception e){
                return new JSONResponse(500, "Wrong code");
            }
            if(!token.isEmpty()){
                userRepository.updateHesToken(auth.getName(), token);
            }else{
                return new JSONResponse(500, "An unknown error occured.");
            }
            return new JSONResponse(200, "Sent SMS");
        }

        @Transactional(propagation = Propagation.REQUIRED)
        @PostMapping(path="/api/whereby/url")
        public JSONResponse postWherebyApiKey(@RequestBody String body, Authentication auth){
           userRepository.updateWherebyKey(auth.getName(), body);
           return new JSONResponse(200, "Success");
        }

        @Transactional(propagation = Propagation.REQUIRED)
        @GetMapping(path="/api/hes/check")
        public JSONResponse getHesCheck(@RequestParam(value = "id") long id, Authentication auth){
            User doctor = userRepository.findByEmail(auth.getName());
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

class PatientsResponse implements StandardResponse{
    public final long patient_num;
    public final List<User> patients;
    public PatientsResponse(long num, List<User> patients){
        this.patient_num = num;
        
        this.patients = patients;
    }
}
