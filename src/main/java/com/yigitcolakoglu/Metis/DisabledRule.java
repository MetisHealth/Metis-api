package com.yigitcolakoglu.Metis;

import java.util.Calendar;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
public class DisabledRule{

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private final long id; 

    private final Calendar start; //date where the rule starts applying from.
    @ManyToOne
    private final User doctor; //date where the rule starts applying from.
    private final long duration; // time in millis which this rule is applied for
    private final long repetition; // interval in millis which this rule is repeated

    public DisabledRule(long id, Calendar start, long duration, long repetition, User doctor){
        this.start = start;
        this.duration = duration;
        this.repetition = repetition;
        this.id = id;
        this.doctor = doctor;
    }
    
    @JsonIgnore
    public boolean checkDate(Calendar date){
        long dateMillis = date.getTimeInMillis(); 
        long timeBetween = dateMillis - start.getTimeInMillis();
        if(timeBetween < 0){
            return false;
        }
        long normalizedTime = timeBetween % (duration + repetition);
        return repetition < normalizedTime;
    }

    // getters
    public long getId(){ return this.id; }
    public Calendar getStart()  { return this.start; }
    public long getDuration()  { return this.duration; }
    public long getRepetition()  { return this.repetition; }
}
