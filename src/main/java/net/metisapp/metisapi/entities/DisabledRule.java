package net.metisapp.metisapi.entities;

import java.util.Calendar;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
public class DisabledRule{ 

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id; 

    private Calendar start; //date where the rule starts applying from.

    @ManyToOne
    @JsonIgnore
    private MetisUser doctor; //date where the rule starts applying from.
    private long duration; // time in millis which this rule is applied for
    private String name; // time in millis which this rule is applied for
    private long repetition; // interval in millis which this rule is repeated

    
    @JsonIgnore
    public boolean checkDate(Calendar date){
        long dateMillis = date.getTimeInMillis(); 
        long timeBetween = dateMillis - start.getTimeInMillis();
        if(timeBetween < 0){
            return false;
        }
        long normalizedTime = timeBetween % (duration + repetition);
        return normalizedTime < duration;
    }

    // getters
    public long getId(){ return this.id; }
    public String getName(){ return this.name; }
    public Calendar getStart()  { return this.start; }
    public long getDuration()  { return this.duration; }
    public long getRepetition()  { return this.repetition; }

    public void setDoctor(MetisUser doctor)  { this.doctor=doctor; }
}
