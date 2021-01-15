package com.yigitcolakoglu.Metis;

public class JSONResponse implements StandardResponse{
    private int code;
    private String message;
    private Appointment appointment;

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
    //  getters 
    public int getCode(){ return this.code; }
    public String getMessage() {return this.message; }
    public Appointment getAppointment() {return this.appointment; }
}
