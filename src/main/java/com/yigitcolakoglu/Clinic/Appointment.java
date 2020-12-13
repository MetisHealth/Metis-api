package com.yigitcolakoglu.Clinic;

import java.util.Date;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import  java.util.List;

@Entity
public class Appointment{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;  

    @JsonFormat(locale = "tr", shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy, HH:mm", timezone = "Europe/Istanbul")
    private Date start;
    @JsonFormat(locale = "tr", shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy, HH:mm", timezone = "Europe/Istanbul")
    private Date end;

    private double price;
    private boolean receipt;
    private boolean online;
    private String zoom_url;

    @ManyToOne
    private User patient;

    @JsonIgnore
    @ManyToOne
    private User doctor;
   
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
    public long getId()    { return this.id; }
    public User getPatient()    { return this.patient; }
    public User getDoctor()    { return this.doctor; }
    public boolean getReceipt()    { return this.receipt; }
    public boolean getOnline()    { return this.online; }
    public double getPrice()    { return this.price; }

    // setters
    public void setDoctor(User doctor)    { this.doctor = doctor; }
}
