package com.yigitcolakoglu.Clinic;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.Entity;
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
public class User{

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;
    
    private String role;

    private String name;
    private String TC_no;
    private String HES_code;
    private String phone;

    private boolean safe;

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
    protected boolean enabled, expiredCreds, expired, locked = false;
   
    @JsonIgnore
    private boolean checkSafe(){
        return true;
    }
    // getters
    public String getName()        { return this.name;     }
    public String getTCNo()        { return this.TC_no;    }
    public String getPassword()    { return this.password;    }
    public String getEmail()       { return this.email;    }
    public String getPhone()       { return this.phone;    }
    @JsonIgnore
    public List<DisabledRule> getDisabled(){ return this.disabledRules; }
    public String getHESCode()             { return this.HES_code; }
    public long getId()                    { return this.id;       }
    public boolean getEnabled()            { return this.enabled;       }
    public boolean getSafe()            { return this.safe;       }
    public String getRole()                { return this.role;       }

    // setters
    public void setName(String name)        { this.name = name;         }
    public void setDoctor(User doctor)      { this.doctor = doctor;         }
    public void setPassword(User doctor)      { this.doctor = doctor;         }
    public void setTCNo(String TC_no)       { this.TC_no = TC_no;       }
    public void setEmail(String email)      { this.email = email;       }
    public void setPhone(String phone)      { this.phone = phone;       }
    public void setHESCode(String HES_code) { this.HES_code = HES_code; }



}
