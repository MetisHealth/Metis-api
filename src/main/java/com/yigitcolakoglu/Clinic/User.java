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

import java.util.Collection;
import java.util.List;

@Entity
public class User{

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
    
    private String role;

    private String name;
    private String TC_no;
    private String HES_code;
    private String phone;

    @OneToMany(mappedBy="patient")
    private List<Appointment> patientAppointments;

    @OneToMany(mappedBy="doctor")
    private List<Appointment> doctorAppointments;

    @OneToMany(mappedBy="doctor")
    private List<User> patients;
    
    @OneToMany(mappedBy="doctor")
    private List<DisabledRule> disabledRules;

    @ManyToOne
    private User doctor; 

    @Column(nullable=false)
    protected String password;

    @Column(unique = true, nullable=false)
    protected String email;

    protected boolean enabled, expiredCreds, expired, locked = false;
    
    // getters
    public String getName()    { return this.name;     }
    public String getTCNo()    { return this.TC_no;    }
    public String getPassword()    { return this.password;    }
    public String getEmail()   { return this.email;    }
    public String getPhone()   { return this.phone;    }
    public List<DisabledRule> getDisabled(){ return this.disabledRules; }
    public String getHESCode() { return this.HES_code; }
    public long getId()        { return this.id;       }
    public boolean getEnabled()        { return this.enabled;       }
    public String getRole()        { return this.role;       }

    // setters
    public void setName(String name)        { this.name = name;         }
    public void setTCNo(String TC_no)       { this.TC_no = TC_no;       }
    public void setEmail(String email)      { this.email = email;       }
    public void setPhone(String phone)      { this.phone = phone;       }
    public void setHESCode(String HES_code) { this.HES_code = HES_code; }



}
