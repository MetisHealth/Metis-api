package net.metisapp.metisapi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Entity
@Table(name="users")
public class MetisUser{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;
    
    private String role = "PATIENT";

    private String name;
    private String TC_no = "";
    private String HES_code = "";
    private String phone = "";

    @NonNull
    @Column(columnDefinition = "TEXT")
    @Convert(converter = ProtocolNumberJSONConverter.class)
    private HashMap<String, Calendar> protocolNumbers;

    @JsonIgnore
    @Column( length = 999 )
    private String zoom_access_token;
    @JsonIgnore
    @Column( length = 999 )
    private String zoom_refresh_token;
    @JsonIgnore
    private String HES_id_token;
    @JsonIgnore
    private Calendar zoom_expiry = Calendar.getInstance();
    @JsonIgnore
    private Calendar HES_expiry = Calendar.getInstance();

    private boolean zoom_api_works = false;
    private boolean HES_api_works = false;

    private String locale = "tr";
    private boolean safe = true;

    @JsonIgnore @OneToMany(cascade={CascadeType.REMOVE}, mappedBy="patient")
    private List<Appointment> patientAppointments;

    @JsonIgnore @OneToMany(cascade={CascadeType.REMOVE}, mappedBy="doctor")
    private List<Appointment> doctorAppointments;

    @JsonIgnore @OneToMany(mappedBy="doctor")
    private List<MetisUser> patients;
    
    @OneToMany(cascade={CascadeType.REMOVE}, mappedBy="doctor")
    private List<DisabledRule> disabledRules;

    @JsonIgnore @ManyToOne(fetch = FetchType.LAZY)
    private MetisUser doctor;

    protected String password;

    @Column(unique = true, nullable=false)
    protected String email = "";

    @JsonIgnore
    private boolean checkSafe(){
        return true;
    }

    @Override
    public boolean equals(Object p){
        if (!(p instanceof MetisUser))
            return false;
        MetisUser u = (MetisUser) p;
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
    @JsonIgnore
    public String getPassword()    { return this.password;    }
    public HashMap<String, Calendar> getProtocolNumbers()    { return this.protocolNumbers;    }
    public String getEmail()       { return this.email;    }
    public String getPhone()       { return this.phone;    }
    @JsonIgnore
    public List<DisabledRule> getDisabled(){ return this.disabledRules; }
    @JsonIgnore 
    public String getHesToken() { return this.HES_id_token; }
    public String getHESCode()             { return this.HES_code; }
    @JsonIgnore
    public MetisUser getDoctor() { return this.doctor; }
    public long getId()                    { return this.id;       }
    public boolean getSafe()            { return this.safe;       }
    public String getRole()                { return this.role;       }

    // setters
    public void setName(String name)        { this.name = name;         }
    public void setDoctor(MetisUser doctor)      { this.doctor = doctor;         }
    @JsonProperty("password")
    public void setPassword(String password)      { this.password = password;         }
    public void setTCNo(String TC_no)       { this.TC_no = TC_no;       }
    public void setEmail(String email)      { this.email = email;       }
    public void setPhone(String phone)      { this.phone = phone;       }
    public void setHESCode(String HES_code) { this.HES_code = HES_code; }



}
