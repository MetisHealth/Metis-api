package net.metisapp.metisapi.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.metisapp.metisapi.endpoints.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.PrototypeAspectInstanceFactory;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.*;

public class ProtocolNumberJSONConverter implements AttributeConverter<Map<String, Calendar>,String> {
	private final Logger log = LoggerFactory.getLogger(Profile.class);

	@Override
	public String convertToDatabaseColumn(Map<String, Calendar> protocolNumbers) {
		if(protocolNumbers == null)
			return "[]";
		String protocolNumberJson = null;
		try {
			protocolNumberJson = new ObjectMapper().writeValueAsString(protocolNumbers);
		} catch (final JsonProcessingException e) {
			log.error("JSON writing error", e);
		}

		return protocolNumberJson;
	}

	@Override
	public Map<String, Calendar> convertToEntityAttribute(String jsonString) {
		if(jsonString == null)
			return new HashMap<String, Calendar>();
		Map<String, Calendar> protocolNumbers = null;
		try {
			protocolNumbers = new ObjectMapper().readValue(jsonString, Map.class);
		} catch (final IOException e) {
			log.error("JSON reading error", e);
		}

		return protocolNumbers;
	}

}
