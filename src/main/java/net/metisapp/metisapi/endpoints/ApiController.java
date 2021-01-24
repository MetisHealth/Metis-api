package net.metisapp.metisapi.endpoints;

import java.lang.Exception;
import io.sentry.Sentry;

import net.metisapp.metisapi.entities.Appointment;
import net.metisapp.metisapi.entities.DisabledRule;
import net.metisapp.metisapi.entities.MetisUser;
import net.metisapp.metisapi.repositories.AppointmentRepository;
import net.metisapp.metisapi.repositories.DisabledRuleRepository;
import net.metisapp.metisapi.repositories.ProtocolNumberRepository;
import net.metisapp.metisapi.repositories.UserRepository;
import net.metisapp.metisapi.responses.JSONResponse;
import net.metisapp.metisapi.responses.StandardResponse;
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

import net.metisapp.metisapi.entities.ProtocolNumber;

@RestController
public class ApiController {
        // TODO Session management 
        @Autowired
        private AppointmentRepository appointmentRepository;




}

