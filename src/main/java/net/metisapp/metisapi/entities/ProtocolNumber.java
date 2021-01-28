package net.metisapp.metisapi.entities;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;


import java.util.Calendar;

public class ProtocolNumber{
    public Long number;

    public Calendar addedDate = Calendar.getInstance();
    
    @Override
    public boolean equals(Object p){
        if (!(p instanceof ProtocolNumber))
            return false;
        return this.number == ((ProtocolNumber)p).number;
    }
}
