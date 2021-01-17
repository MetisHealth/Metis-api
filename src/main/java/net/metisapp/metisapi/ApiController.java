package net.metisapp.metisapi;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.GrantedAuthority;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper; 
import com.fasterxml.jackson.databind.node.ObjectNode; 

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.UUID;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class ApiController {
        // TODO Session management 

        private final int MAX_APPOINTMENT_RESPONSE_SIZE = 1000;
        private final Logger log = LoggerFactory.getLogger(ApiController.class);

        @Autowired
        private AppointmentRepository appointmentRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ProtocolNumberRepository protocolNumberRepository;

        @Autowired
        private DisabledRuleRepository disabledRuleRepository;
        
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

            User doctor = userRepository.findByEmail(auth.getName());
            List<Appointment> appointments = appointmentRepository.findAllBetweenDoctor(start, end, doctor); 

            if(start.after(end) || start.equals(end)) return appointments;

            if( appointments.size() > MAX_APPOINTMENT_RESPONSE_SIZE ){ // Do not return a response in case the range is too big. Prevents DOS.
                appointments.clear();
            }

            return appointments;
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
            User doctor;

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
        private boolean checkOverlap(User doc, Calendar start, Calendar end){
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

        private String capitalizeFirstLetters(String phrase){
            String[] words = phrase.trim().toLowerCase().split(" ");
            String finalPhrase = "";
            for(int i = 0; i < words.length; i++){
                finalPhrase += words[i].substring(0, 1).toUpperCase() + words[i].substring(1) + " ";
            }
            return finalPhrase.trim();
        }

        @GetMapping("/patients")
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
        @PostMapping(path="/patients/create")
        public JSONResponse postPatientCreate(@RequestBody String body, Authentication auth){
            if(checkRole(auth.getAuthorities(), "PATIENT")){
               return new JSONResponse(403, "You are not authorized to do this!"); 
            }

            // TODO Implement CSRF Protection
            User patient;
            try{ // Parse the POST request body into an Appointment object.
                patient = new ObjectMapper().readValue(body, User.class);
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

        @PostMapping(path="/password")
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
        
        @PostMapping(path="/patients/update")
        @Transactional(propagation = Propagation.REQUIRED)
        public JSONResponse postPatientUpdate(@RequestBody String body, Authentication auth){

            if(checkRole(auth.getAuthorities(), "PATIENT")){
               return new JSONResponse(403, "You are not authorized to do this!"); 
            }
            User patient;
            try{ // Parse the POST request body into an Appointment object.
                patient = new ObjectMapper().readValue(body, User.class);
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
                int edited = this.userRepository.updatePatient(patient.getName(), patient.getEmail(), patient.getPhone(), patient.getTCNo(), patient.getHESCode(), patient.getRole(), patient.getId());
            }catch(DataIntegrityViolationException ex){
                return new JSONResponse(500, "A user with that e-mail already exists!");
            }

            List<ProtocolNumber> newProtocolNumbers = new LinkedList<ProtocolNumber>(patient.getProtocolNumbers());
            List<ProtocolNumber> oldProtocolNumbers = protocolNumberRepository.getAllProtocolNumbers(this.userRepository.findById(patient.getId()));
            for(ProtocolNumber pnum : patient.getProtocolNumbers()){ 
               if(oldProtocolNumbers.contains(pnum)){
                   newProtocolNumbers.remove(pnum);
                   oldProtocolNumbers.remove(pnum);
               }else{
                   if(protocolNumberRepository.getOwner(pnum.number) != null) newProtocolNumbers.remove(pnum);
               } 
            }
            for(ProtocolNumber p : newProtocolNumbers)
                p.patient = this.userRepository.findById(patient.getId());
            protocolNumberRepository.deleteAll(oldProtocolNumbers);
            protocolNumberRepository.saveAll(newProtocolNumbers);

            return new JSONResponse(200, "Success");
        } 
        
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        private void deletePatientAppointments(User patient){
            this.appointmentRepository.deletePatientsAppointments(patient);
        }

        @PostMapping(path="/patients/delete")
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public JSONResponse postPatientDelete(@RequestBody String body, Authentication auth){
            if(checkRole(auth.getAuthorities(), "PATIENT")){
               return new JSONResponse(403, "You are not authorized to do this!"); 
            }
            User patient;
            try{ // Parse the POST request body into an Appointment object.
                patient = new ObjectMapper().readValue(body, User.class);
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
                this.protocolNumberRepository.deleteAll(this.protocolNumberRepository.getAllProtocolNumbers(this.userRepository.findById(patient.getId())));
                this.userRepository.delete(this.userRepository.findById(patient.getId()));
            }catch(DataIntegrityViolationException ex){
                return new JSONResponse(500, "An unknown error occured."); //TODO Better error message
            }
            return new JSONResponse(200, "Success");
        } 

        @GetMapping(path="/profile")
        public JSONResponse getProfile(Authentication auth){
            if( auth == null)
                return new JSONResponse(503, "You are not logged in!");
            return new JSONResponse(200, "Logged in.", userRepository.findByEmail(auth.getName()));
        } 
        
        @PostMapping(path="/profile/update")
        @Transactional(propagation = Propagation.REQUIRED)
        public StandardResponse postProfileUpdate(@RequestBody String body, Authentication auth){
            User profile;
            try{ // Parse the POST request body into an Appointment object.
                profile = new ObjectMapper().readValue(body, User.class);
            }catch(Exception ex){
                return new JSONResponse(500, "Server could not parse that. This could be because you submitted invalid data.");
            }
            profile.setName(capitalizeFirstLetters(profile.getName()));
            try{
                int edited = this.userRepository.updateUser(profile.getName(), profile.getEmail(), profile.getPhone(), profile.getTCNo(),profile.getHESCode(), profile.getLocale(), userRepository.findByEmail(auth.getName()).getId());
            }catch(DataIntegrityViolationException ex){
                return new JSONResponse(500, "A user with that e-mail already exists!");
            }
            return new JSONResponse(200, "Success");
        }

        @GetMapping(path="/hes/sendsms")
        public JSONResponse sendHesSMS(Authentication auth){ // TODO Error handling
            String phone = userRepository.findByEmail(auth.getName()).getPhone();
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode request_obj = mapper.createObjectNode();
            request_obj.put("phone",phone);
            HttpEntity<String> request = new HttpEntity<String>(request_obj.toString(), headers);
            ResponseEntity<String> result = restTemplate.postForEntity("https://hessvc.saglik.gov.tr/send-code-to-login", request, String.class); 
            return new JSONResponse(200, "Sent SMS");
        }

        @GetMapping(path="/hes/smscode")
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
                ResponseEntity<String> result = restTemplate.postForEntity("https://hessvc.saglik.gov.tr/authenticate-with-code", request, String.class); 
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
        @PostMapping(path="/whereby/url")
        public JSONResponse postWherebyApiKey(@RequestBody String body, Authentication auth){
           userRepository.updateWherebyKey(auth.getName(), body);
           return new JSONResponse(200, "Success");
        }

        @Transactional(propagation = Propagation.REQUIRED)
        @GetMapping(path="/hes/check")
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
