package net.metisapp.metisapi.entities;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;


import java.util.Calendar;

@Entity
@Table(name="protocol_numbers")
public class ProtocolNumber{
    @Id
    public long number;

    @JsonIgnore
    @ManyToOne
    public MetisUser patient;
    public Calendar addedDate = Calendar.getInstance();
    
    @Override
    public boolean equals(Object p){
        if (!(p instanceof ProtocolNumber))
            return false;
        return this.number == ((ProtocolNumber)p).number;
    }
}
