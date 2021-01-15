$.ajaxSetup({
      contentType: "application/json; charset=utf-8"
});

function display_toast(msg, data){
    if(data.code == 200){
        toastr.success(msg);
    }else{
        toastr.error(data.message);
    }
}
let Patient = class {
    constructor(id, name, phone, mail, tc, hes, safe, pnums, role){
        this.role = role
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = mail;
        this.tcno = tc;
        this.hescode = hes;
        this.safe = safe;
        this.protocolNumbers = pnums;
    }

    create(f){
        delete this.id;
        $.post("/api/patients/create", JSON.stringify(this), function(data){
            f();
            display_toast("Patient created successfully", data);
        }, "json");
    }

    update(f){
        $.post("/api/patients/update", JSON.stringify(this), function(data){
            f();
            display_toast("Patient updated successfully", data);
        }, "json");
    }

    delete(f){
        $.post("/api/patients/delete", JSON.stringify(this), function(data){
            f();
            display_toast("Patient deleted successfully", data);
        }, "json");

    }

    static from(json){
        return Object.assign(new Patient(null, null, null, null, null, null, null, null), json);
    }

}

let Appointment = class{
    constructor(id, start, end, patient, online, receipt){
        this.id = id;
        this.receipt = receipt;
        this.start = start;
        this.end = end;
        this.online = online;
        this.patient = patient;
    }

    create(){
        delete this.id;
        $.post("/api/appointments", JSON.stringify(this), function(data){
            f();
            display_toast("Appointment created successfully", data);
        }, "json");
    }

    static from(json){
        return Object.assign(new Appointment(null, null, null, null, null), json);
    }
}
