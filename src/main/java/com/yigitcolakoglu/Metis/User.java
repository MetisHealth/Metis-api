package com.yigitcolakoglu.Metis;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;

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

import java.util.Collection;
import java.util.List;

@Entity
@Table(name="users")
public class User{

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;
    
    private String role;

    private String name;
    private String TC_no = "00000000000";
    private String HES_code;
    private String phone;

    private boolean HES_api_works = false;
    @JsonIgnore 
    private String HES_id_token;

    private boolean WhereBy_api_works = false;
    private String WhereBy_URL = "";

    private String locale = "tr";
    private boolean safe = true;

    @JsonIgnore @OneToMany(mappedBy="patient")
    private List<Appointment> patientAppointments;

    @JsonIgnore @OneToMany(mappedBy="doctor")
    private List<Appointment> doctorAppointments;

    @JsonIgnore @OneToMany(mappedBy="doctor")
    private List<User> patients;
    
    @JsonIgnore @OneToMany(mappedBy="doctor")
    private List<DisabledRule> disabledRules;

    @JsonIgnore @ManyToOne
    private User doctor; 

    @JsonIgnore
    protected String password;

    @Column(unique = true, nullable=false)
    protected String email;

    @JsonIgnore
    private boolean checkSafe(){
        return true;
    }
    // getters
    public String getName()        { return this.name;     }
    public String getTCNo()        { return this.TC_no;    }
    public String getLocale()        { return this.locale;    }
    public String getPassword()    { return this.password;    }
    public String getEmail()       { return this.email;    }
    public String getPhone()       { return this.phone;    }
    @JsonIgnore
    public List<DisabledRule> getDisabled(){ return this.disabledRules; }
    @JsonIgnore 
    public String getHesToken() { return this.HES_id_token; }
    public String getHESCode()             { return this.HES_code; }
    public String getWherebyUrl()             { return this.WhereBy_URL; }
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
