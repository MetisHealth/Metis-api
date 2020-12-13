package com.yigitcolakoglu.Clinic;

import com.yigitcolakoglu.Clinic.Appointment;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataIntegrityViolationException;

import javax.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper; 

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.text.ParseException;

@RestController
public class ApiController {
        // TODO Session management 

        private final int MAX_APPOINTMENT_RESPONSE_SIZE = 1000;

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
                System.err.println(ex.toString());
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
                System.err.println(ex.toString());
                return new JSONResponse(500, "Server could not parse that. This could be because you submitted invalid data.");
            }
            // TODO check if doctor ids match with cookie
            boolean overlapped = appointmentRepository.findOverlaps(appointment.getStart(), appointment.getEnd(), appointment.getDoctor()); // Check for overlaps in the submitted dates.
            appointment.setDoctor(userRepository.findByEmail(auth.getName()));

            if(overlapped){
                return new JSONResponse(400, "Dates you have submitted overlaps with already existing ones.");
            }

            if(userRepository.checkDoctor(appointment.getPatient().getId(), appointment.getDoctor()).size() != 1){
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
                System.err.println(ex.toString());
                return new JSONResponse(500, "Server could not parse that. This could be because you submitted invalid data.");
            }  
            try{
                this.appointmentRepository.deleteAppointment(appointment.getId(), userRepository.findByEmail(auth.getName()));
            }catch(DataIntegrityViolationException ex){
                System.out.println(ex.toString());
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
        public PatientsResponse getPatient(@RequestParam(value="email") String email, @RequestParam(value="phone") String phone, @RequestParam(value="name") String name, @RequestParam(value="page", defaultValue="0") int page, @RequestParam(value="psize", defaultValue="20") int psize, Authentication auth) {
            Pageable pageable = PageRequest.of(page < 0 ? 0 : page, psize);
            
            User doctor = userRepository.findByEmail(auth.getName());
            List<User> patients = userRepository.searchUser(email, phone, name.isEmpty() ? "" : capitalizeFirstLetters(name), doctor, "PATIENT", pageable); // TODO Dynamic doctor
            
            if(patients.size() > 1000){
                patients.clear();
            }
            long patient_num = userRepository.countPatients(email, phone, name.isEmpty() ? "" : capitalizeFirstLetters(name), doctor, "PATIENT");
            return new PatientsResponse(patient_num, patients); // TODO Write a method in UserRepository to get the number of patients. As this definitely won't work. 
        }

        @PostMapping(path="/api/patients/create")
        public JSONResponse postPatientCreate(@RequestBody String body, Authentication auth){
            // TODO Implement CSRF Protection
            User patient;
            try{ // Parse the POST request body into an Appointment object.
                patient = new ObjectMapper().readValue(body, User.class);
            }catch(Exception ex){
                System.err.println(ex.toString());
                return new JSONResponse(500, "Server could not parse that. This could be because you submitted invalid data.");
            }
            patient.setDoctor(userRepository.findByEmail(auth.getName()));
            patient.setName(capitalizeFirstLetters(patient.getName()));
            try{
                this.userRepository.save(patient);
            }catch(DataIntegrityViolationException ex){
                System.err.println(ex.toString());
                return new JSONResponse(500, "A user with that e-mail already exists!");
            }
            return new JSONResponse(200, "Success");
        } 

        @PostMapping(path="/api/patients/update")
        @Transactional(propagation = Propagation.REQUIRED)
        public JSONResponse postPatientUpdate(@RequestBody String body, Authentication auth){
            // TODO Implement CSRF Protection
            User patient;
            try{ // Parse the POST request body into an Appointment object.
                patient = new ObjectMapper().readValue(body, User.class);
            }catch(Exception ex){
                System.err.println(ex.toString());
                return new JSONResponse(500, "Server could not parse that. This could be because you submitted invalid data.");
            }  
            patient.setName(capitalizeFirstLetters(patient.getName()));
            try{
                int edited = this.userRepository.updatePatient(patient.getName(), patient.getEmail(), patient.getPhone(), patient.getTCNo(),patient.getHESCode(), patient.getId(), userRepository.findByEmail(auth.getName()));
            }catch(DataIntegrityViolationException ex){
                System.out.println(ex.toString());
                return new JSONResponse(500, "A user with that e-mail already exists!");
            }
            return new JSONResponse(200, "Success");
        } 

        @PostMapping(path="/api/patients/delete")
        @Transactional(propagation = Propagation.REQUIRED)
        public JSONResponse postPatientDelete(@RequestBody String body, Authentication auth){
            // TODO Implement CSRF Protection
            // TODO more code to delete patients who have appointments
            User patient;
            try{ // Parse the POST request body into an Appointment object.
                patient = new ObjectMapper().readValue(body, User.class);
            }catch(Exception ex){
                System.err.println(ex.toString());
                return new JSONResponse(500, "Server could not parse that. This could be because you submitted invalid data.");
            }  
            patient.setName(capitalizeFirstLetters(patient.getName()));
            try{
                this.userRepository.deletePatient(patient.getId(), userRepository.findByEmail(auth.getName()));
            }catch(DataIntegrityViolationException ex){
                System.out.println(ex.toString());
                return new JSONResponse(500, "A user with that e-mail already exists!");
            }
            return new JSONResponse(200, "Success");
        } 
}

class PatientsResponse{
    public final long patient_num;
    public final List<User> patients;
    public PatientsResponse(long num, List<User> patients){
        this.patient_num = num;
        this.patients = patients;
    }
}
