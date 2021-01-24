package net.metisapp.metisapi;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.Column;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper; 
import com.fasterxml.jackson.databind.node.ObjectNode; 

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collection;
import java.util.Calendar;
import java.util.List;

@Entity
@Table(name="protocol_numbers")
class ProtocolNumber{
    @Id
    public long number;

    @JsonIgnore
    @ManyToOne
    public User patient;
    public Calendar addedDate = Calendar.getInstance();
    
    @Override
    public boolean equals(Object p){
        if (!(p instanceof ProtocolNumber))
            return false;
        return this.number == ((ProtocolNumber)p).number;
    }
}
