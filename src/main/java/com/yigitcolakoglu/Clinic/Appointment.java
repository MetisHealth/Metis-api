package com.yigitcolakoglu.Clinic;

import java.util.Date;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import  java.util.List;

@Entity
public class Appointment{
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private final long id;  
    private final Date start;
    private final Date end;

    @ManyToOne
    private final User patient;
    
    @ManyToOne
    private final User doctor;
    
    public Appointment(long id, Date start, Date end, User patient, User doctor){
        this.id = id;
        this.start = start;
        this.end = end;
        this.patient = patient;
        this.doctor = doctor;
    }

    @JsonIgnore
    public boolean checkDisabled(){
        List<DisabledRule> rules = doctor.getDisabled();
        for(int i=0; i<rules.size(); i++){
            if(rules.get(i).checkDate(start)){
                return true;
            }   
        }
        return false;
    }

    // getters
    public Date  getStart()   { return this.start; }
    public Date  getEnd()     { return this.end; }
    public User getPatient()    { return this.patient; }
    public User getDoctor()    { return this.doctor; }
}
