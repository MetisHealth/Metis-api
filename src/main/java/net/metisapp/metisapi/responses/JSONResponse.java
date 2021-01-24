package net.metisapp.metisapi.responses;

import net.metisapp.metisapi.entities.Appointment;
import net.metisapp.metisapi.entities.MetisUser;

public class JSONResponse implements StandardResponse{
    private int code;
    private String message;
    private Appointment appointment;
    private MetisUser user;

    public JSONResponse(int code, String message){
        this.code = code;
        this.message = message;
    }
    
    public JSONResponse(int code, Appointment app){
        this.code = code;
        this.appointment = app;
    }
    
    public JSONResponse(int code, String msg, Appointment app){
        this.code = code;
        this.appointment = app;
        this.message = msg;
    }

    public JSONResponse(int code, String msg, MetisUser u){
        this.code = code;
        this.user = u;
        this.message = msg;
    }
    //  getters 
    public int getCode(){ return this.code; }
    public String getMessage() {return this.message; }
    public Appointment getAppointment() {return this.appointment; }
    public MetisUser getUser() {return this.user; }
}
