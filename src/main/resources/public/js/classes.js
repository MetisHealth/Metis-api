let Patient = class {
    constructor(id, name, phone, mail, tc, hes){
        this.role = "PATIENT"
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = mail;
        this.tcno = tc;
        this.hescode = hes;
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
        return Object.assign(new Patient(null, null, null, null, null), json);
    }

}
