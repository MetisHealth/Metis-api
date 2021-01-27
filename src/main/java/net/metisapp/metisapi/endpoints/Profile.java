package net.metisapp.metisapi.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.metisapp.metisapi.entities.MetisUser;
import net.metisapp.metisapi.repositories.DisabledRuleRepository;
import net.metisapp.metisapi.repositories.UserRepository;
import net.metisapp.metisapi.responses.JSONResponse;
import net.metisapp.metisapi.responses.StandardResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.metrics.annotation.Timed;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@Timed
public class Profile {
	@Autowired
	private UserRepository userRepository;

	private String capitalizeFirstLetters(String phrase) {
		String[] words = phrase.trim().toLowerCase().split(" ");
		String finalPhrase = "";
		for (int i = 0; i < words.length; i++) {
			finalPhrase += words[i].substring(0, 1).toUpperCase() + words[i].substring(1) + " ";
		}
		return finalPhrase.trim();
	}
		private final Logger log = LoggerFactory.getLogger(Profile.class);

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

	@PostMapping(path="/profile/password")
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

	@GetMapping(path="/profile")
	public JSONResponse getProfile(Authentication auth){
		if( auth == null)
			return new JSONResponse(503, "You are not logged in!");
		return new JSONResponse(200, "Logged in.", userRepository.findByEmail(auth.getName()));
	}

	@PostMapping(path="/profile/update")
	@Transactional(propagation = Propagation.REQUIRED)
	public StandardResponse postProfileUpdate(@RequestBody String body, Authentication auth){
		MetisUser profile;
		try{ // Parse the POST request body into an Appointment object.
			profile = new ObjectMapper().readValue(body, MetisUser.class);
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

	@GetMapping(path="/profile/hes/sendsms")
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

	@GetMapping(path="/profile/smscode")
	@Transactional(propagation = Propagation.REQUIRED)
	public JSONResponse sendHesSMS(@RequestParam(value = "code") String smsCode , Authentication auth){ // TODO Error handling
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

}
