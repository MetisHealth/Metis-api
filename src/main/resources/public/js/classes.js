$.ajaxSetup({
      contentType: "application/json; charset=utf-8"
});

let Patient = class {
    constructor(id, name, phone, mail, tc, hes, safe, role){
        this.role = role
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = mail;
        this.tcno = tc;
        this.hescode = hes;
        this.safe = safe;
    }

    create(){
        delete this.id;
        $.post("/api/patients/create", JSON.stringify(this), function(data){
            console.log(data);
        }, "json");
    }

    update(){
        $.post("/api/patients/update", JSON.stringify(this), function(data){
            console.log(data);
        }, "json");
    }

    delete(){
        $.post("/api/patients/delete", JSON.stringify(this), function(data){
            console.log(data);
        }, "json");

    }

    static from(json){
        return Object.assign(new Patient(null, null, null, null, null, null, null), json);
    }

}

let Appointment = class{
    constructor(id, start, end, patient, price, online, receipt){
        this.id = id;
        this.receipt = receipt;
        this.start = start;
        this.end = end;
        this.price = price;
        this.online = online;
        this.patient = patient;
    }

    create(){
        delete this.id;
        $.post("/api/appointments", JSON.stringify(this), function(data){
            console.log(data);
        }, "json");
    }

    static from(json){
        return Object.assign(new Appointment(null, null, null, null, null), json);
    }
}
