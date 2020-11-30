package com.yigitcolakoglu.Clinic;

public class JSONResponse{
    private final int code;
    private final String message;

    public JSONResponse(int code, String message){
        this.code = code;
        this.message = message;
    }

    //  getters 
    public int getCode(){ return this.code; }
    public String getMessage() {return this.message; }
}
