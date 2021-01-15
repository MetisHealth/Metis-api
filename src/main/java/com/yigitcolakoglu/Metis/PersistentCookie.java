package com.yigitcolakoglu.Metis;

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
@Table(name="persistent_logins")
class PersistentCookie{
    @Id
    @Column(length=64, nullable=false)
    public String series;

    @Column(length=64, nullable=false)
    public String username;

    @Column(length=64, nullable=false)
    public String token;

    @Column(nullable=false)
    public Calendar last_used;
}
