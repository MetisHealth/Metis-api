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
import javax.servlet.http.HttpServletRequest;
import com.google.gson.Gson;

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
            start.setHours(0);
            start.setMinutes(0);
            start.setSeconds(0);

            Date end = new Date(); 
            end.setHours(23);
            end.setMinutes(59);
            end.setSeconds(59);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
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
        public JSONResponse postAppointment(@RequestBody String body){
            // TODO Implement CSRF Protection
            Appointment appointment;
            try{ // Parse the POST request body into an Appointment object.
                appointment = new Gson().fromJson(body, Appointment.class);
            }catch(Exception ex){
                System.err.println(ex.toString());
                return new JSONResponse(500, "Server could not parse that. This could be because you submitted invalid data.");
            }
            // TODO check if doctor ids match with cookie
            boolean overlapped = appointmentRepository.findOverlaps(appointment.getStart(), appointment.getEnd(), appointment.getDoctor()); // Check for overlaps in the submitted dates.

            if(overlapped){
                return new JSONResponse(400, "Dates you have submitted overlaps with already existing ones.");
            }

            if(userRepository.checkDoctor(appointment.getPatient().getId(), appointment.getDoctor())){
                return new JSONResponse(500, "This patient either does not exist or is not your patient.");
            } 
            
            if(appointment.checkDisabled()){
                return new JSONResponse(500, "This time is disabled by the doctor.");
            }
            appointmentRepository.save(appointment);
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
            Pageable pageable = PageRequest.of(page, psize);
            
            User doctor = userRepository.findByEmail(auth.getName());
            List<User> patients = userRepository.searchUser(email, phone, name.isEmpty() ? "" : capitalizeFirstLetters(name), doctor, "PATIENT", pageable); // TODO Dynamic doctor
            
            if(patients.size() > 1000){
                patients.clear();
            }

            return new PatientsResponse(patients.size(), patients); // TODO Write a method in UserRepository to get the number of patients. As this definitely won't work. 
        }

        @PostMapping(path="/api/patients")
        public JSONResponse postPatient(@RequestBody String body){
            // TODO Implement CSRF Protection
            User patient;
            try{ // Parse the POST request body into an Appointment object.
                patient = new Gson().fromJson(body, User.class);
            }catch(Exception ex){
                System.err.println(ex.toString());
                return new JSONResponse(500, "Server could not parse that. This could be because you submitted invalid data.");
            }
            // TODO Implement doctor verification 
            patient.setName(capitalizeFirstLetters(patient.getName()));
            // TODO Implement input verification.
            this.userRepository.save(patient);
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
