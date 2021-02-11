package net.metisapp.metisapi.entities;

import java.util.Calendar;


import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;

import  java.util.List;

@Entity
@Table(name="appointments")
public class Appointment{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;  

    private Calendar start;
    private Calendar end;

    private String description = "";
    private double price;
    private boolean receipt;
    private boolean online;
    private String zoom_url;

    @ManyToOne(fetch = FetchType.LAZY)
    private MetisUser patient;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private MetisUser doctor;
   
    @JsonIgnore
    public boolean checkDisabled(){
        List<DisabledRule> rules = doctor.getDisabled();
        for(int i=0; i<rules.size(); i++){
            if(rules.get(i).checkDate(start) || rules.get(i).checkDate(end)){ // Does not work when the appointment contains the rule, but should be fine for most usecases
                return true;
            }   
        }
        return false;
    }

    @Override
    public boolean equals(Object p){
        if (!(p instanceof Appointment))
            return false;
        Appointment a = (Appointment) p;
        return a.getId() == this.id;
    }

    @JsonIgnore
    public boolean dateInAppointment(Calendar time){
        return (time.after(this.start) && time.before(this.end)) || (time.equals(this.start) && time.equals(this.end));
    }
    // getters
    public Calendar getStart()   { return this.start; }
    public Calendar  getEnd()     { return this.end; }
    public String getDescription()     { return this.description; }
    public long getId()    { return this.id; }
    public MetisUser getPatient()    { return this.patient; }
    public MetisUser getDoctor()    { return this.doctor; }
    public boolean getReceipt()    { return this.receipt; }
    public boolean getOnline()    { return this.online; }
    public double getPrice()    { return this.price; }

    // setters
    public void setDoctor(MetisUser doctor)    { this.doctor = doctor; }
}
