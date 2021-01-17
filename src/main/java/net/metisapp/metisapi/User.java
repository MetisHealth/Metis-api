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
import javax.persistence.CascadeType;
import com.fasterxml.jackson.databind.ObjectMapper; 
import com.fasterxml.jackson.databind.node.ObjectNode; 

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collection;
import java.util.Calendar;
import java.util.List;

@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;
    
    private String role;

    private String name;
    private String TC_no = "00000000000";
    private String HES_code;
    private String phone;
    
    @OneToMany(mappedBy="patient")
    private List<ProtocolNumber> protocolNumbers;

    private boolean HES_api_works = false;
    @JsonIgnore 
    private String HES_id_token;

    private boolean WhereBy_api_works = false;
    private String WhereBy_URL = "";

    private String locale = "tr";
    private boolean safe = true;

    @JsonIgnore @OneToMany(cascade={CascadeType.REMOVE}, mappedBy="patient")
    private List<Appointment> patientAppointments;

    @JsonIgnore @OneToMany(cascade={CascadeType.REMOVE}, mappedBy="doctor")
    private List<Appointment> doctorAppointments;

    @JsonIgnore @OneToMany(mappedBy="doctor")
    private List<User> patients;
    
    @OneToMany(cascade={CascadeType.REMOVE}, mappedBy="doctor")
    private List<DisabledRule> disabledRules;

    @JsonIgnore @ManyToOne
    private User doctor; 

    @JsonIgnore
    protected String password;

    @Column(unique = true, nullable=false)
    protected String email = "";

    @JsonIgnore
    private boolean checkSafe(){
        return true;
    }

    @Override
    public boolean equals(Object p){
        if (!(p instanceof User))
            return false;
        User u = (User) p;
        return u.getId() == this.id; 
    }

    @JsonIgnore 
    public boolean checkHesCode(){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer "+this.doctor.getHesToken());
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode request_obj = mapper.createObjectNode();
        request_obj.put("hes_code",this.HES_code);
        HttpEntity<String> request = new HttpEntity<String>(request_obj.toString(), headers);
        String status = "";
        ResponseEntity<String> result = restTemplate.postForEntity("https://hessvc.saglik.gov.tr/services/hescodeproxy/api/check-hes-code", request, String.class); 
        JSONObject response = new JSONObject(result.getBody());
        status = response.getString("current_health_status");
        return status.equals("RISKLESS");
    }

    // getters
    public String getName()        { return this.name;     }
    public String getTCNo()        { return this.TC_no;    }
    public String getLocale()        { return this.locale;    }
    public String getPassword()    { return this.password;    }
    public List<ProtocolNumber> getProtocolNumbers()    { return this.protocolNumbers;    }
    public String getEmail()       { return this.email;    }
    public String getPhone()       { return this.phone;    }
    @JsonIgnore
    public List<DisabledRule> getDisabled(){ return this.disabledRules; }
    @JsonIgnore 
    public String getHesToken() { return this.HES_id_token; }
    public String getHESCode()             { return this.HES_code; }
    public String getWherebyUrl()             { return this.WhereBy_URL; }
    @JsonIgnore
    public User getDoctor() { return this.doctor; }
    public long getId()                    { return this.id;       }
    public boolean getSafe()            { return this.safe;       }
    public String getRole()                { return this.role;       }

    // setters
    public void setName(String name)        { this.name = name;         }
    public void setWherebyUrl(String url) { this.WhereBy_URL = url; }
    public void setDoctor(User doctor)      { this.doctor = doctor;         }
    public void setPassword(User doctor)      { this.doctor = doctor;         }
    public void setTCNo(String TC_no)       { this.TC_no = TC_no;       }
    public void setEmail(String email)      { this.email = email;       }
    public void setPhone(String phone)      { this.phone = phone;       }
    public void setHESCode(String HES_code) { this.HES_code = HES_code; }



}
