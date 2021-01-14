const admin_URL = window.location.protocol + "//" + window.location.host + "/api/patients";
var page_admin = 1;
const pagination_prev_admin = '<li class="page-item page-item-admin"><a class="page-link" href="#" aria-label="Previous"><span aria-hidden="true">&laquo;</span></a></li>'
const pagination_next_admin = '<li class="page-item page-item-admin"><a class="page-link" href="#" aria-label="Next"><span aria-hidden="true">&raquo;</span></a></li>'
const list_item_admin = '\
		  <a href="#" class="list-group-item list-group-item-action flex-column align-items-start">\
			<div class="d-flex w-100 justify-content-between">\
              <div class="mr-auto">\
			    <h5 class="mb-1 mr-auto item-name"></h5>\
			    <div class="mb-1"><span class="item-phone"></span></div>\
		        <small class="item-email"></small>\
              </div>\
              <button type="button" class="patient-button password-patient p-2 bd-highlight btn btn-primary"><i class="fa fa-key" aria-hidden="true"></i></button>\
              <button type="button" class="patient-button delete-patient p-2 bd-highlight btn btn-danger"><i class="fa fa-trash" aria-hidden="true"></i></button>\
			</div>\
		  </a>'


$.ajaxSetup({
      contentType: "application/json; charset=utf-8"
});

function update_users(text){

    let url_parameterized = admin_URL + `?all=yes&role=${$("#admin-role-input").val()}&email=${$("#admin-mail-input").val()}&\
phone=${$("#admin-phone-input").val()}&name=${$("#admin-name-input").val()}&page=${page_admin - 1}&psize=${$("#admin-page-size-input").val()}`

    $.get(url_parameterized, function(data, status){
        console.log(data);
        let page_num = Math.ceil(data.patient_num / parseInt($("#admin-page-size-input").val()));
        if (page_num < page_admin){
            page_admin = page_num;
            update_users("");
        }
        if ( page_num != $(".page-item-admin")/2 - 2 ){
            let foo = 1;

            $(".pagination-admin").empty();
            let prev = $(pagination_prev_admin);
            if(page_admin == 1){
                prev.addClass("disabled");
            }
            prev.appendTo($(".pagination-admin"));
            let item = null;

            do{
                item = $(`<li class="page-item page-item-admin"><a class="page-link" href="#admin-">${foo}</li>`)

                if(page_admin == foo){
                    item = item.addClass("active");
                }

            item.appendTo($(".pagination-admin"))
            foo++;

            } while ( foo <= page_num );

            let next = $(pagination_next_admin);
            if(page_admin == page_num){
                next.addClass("disabled");
            }
            next.appendTo($(".pagination-admin"));

            $(".page-item-admin").on("click", function(event){
                let foo = event.target.innerText;
                if( foo == "«" ){
                    page_admin--;
                }else if( foo == "»" ){
                    page_admin++;
                }else{
                   page_admin = parseInt(foo);
                }
                console.log(page_admin);
                update_users("");
            });
        }

        if (page_admin != parseInt($(".page-item-admin.active").first().text())){
            $(".page-item-admin.active").removeClass("active");

            $(".page-item-admin").eq(page_admin).addClass("active");
            $(".page-item-admin").eq(page_admin + page_num + 2).addClass("active");
        }
        $(".patient-list-admin").empty();
        data.patients.forEach(function(patient){
            let item = $(list_item_admin);
            item.attr("id", `admin-patient-${patient.id}`)
            item.find(".item-name").text(patient.name);
            item.find(".item-phone").text(patient.phone);
            item.find(".item-email").text(patient.email);
            item.find(".delete-patient").on("click", function(e){
                e.stopPropagation();
                Patient.from(patient).delete();
                $(`#admin-patient-${patient.id}`).remove();
            });
            item.find(".password-patient").on("click", function(e){
                e.stopPropagation(); // TODO implement actual password change
                $("#admin-passwordModal").modal("show");
                $("#admin-passwordModalSave").on("click", function(e){
                    let password_change_url = window.location.protocol + "//" + window.location.host + "/api/patient/password";
					$.ajax({
						contentType: 'text/plain',
						data: `newPassword=${$("#admin-passwordModalPassword").val()}&email=${patient.email}`,
						dataType: 'form-data',
						success: function(data){
							console.log(data);
						},
						error: function(){
							console.log("an error occured");
						},
						processData: false,
						type: 'POST',
						url: password_change_url
					});
                });
            });
            item.on("click", function(event){
                $("#admin-patientModalTitle").text("Edit Patient");
                $("#admin-nameInputModal").val(patient.name);
                $("#admin-emailInputModal").val(patient.email);
                $("#admin-phoneInputModal").val(patient.phone);
                $("#admin-hesInputModal").val(patient.hescode);
                $("#admin-roleInputModal").val(patient.role);
                $("#admin-tcInputModal").val(patient.tcno);
                $("#admin-patientModal").modal("show");
                $("#admin-patientModalSave").unbind(); // Clear events for save button
                $("#admin-patientModalSave").on("click", function(event){
                    let new_patient = new Patient(patient.id,
                                              $("#admin-nameInputModal").val(),
                                              $("#admin-phoneInputModal").val(),
                                              $("#admin-emailInputModal").val(),
                                              $("#admin-tcInputModal").val(),
                                              $("#admin-hesInputModal").val(),
                                              1,
                                              patient.protocolNumbers,
                                              $("#admin-roleInputModal").val());
                    new_patient.update(update_users);
                    $("#admin-patientModal").modal("hide");
                });
            });
            item.appendTo($(".patient-list-admin"));
        });
    });

}
$(document).ready(function(){
    update_users("");

    $("#admin-name-input").on('input', update_users);
    $("#admin-role-input").change(update_users);
    $("#admin-phone-input").on('input', update_users);
    $("#admin-mail-input").on('input', update_users);
    $("#admin-page-size-input").on('input', update_users);
    $("#admin-newPatient").on("click", function(event){ // TODO Check for e-mail collisions
        $("#admin-patientModalSave").unbind(); // Clear events for save button
        $("#admin-patientModalTitle").text("New User");
        $("#admin-patientModal input").val("");
        $("#admin-patientModalSave").on("click", function(event){
            let patient = new Patient(null,
                                      $("#admin-nameInputModal").val(),
                                      $("#admin-phoneInputModal").val(),
                                      $("#admin-emailInputModal").val(),
                                      $("#admin-tcInputModal").val(),
                                      $("#admin-hesInputModal").val(),
                                      1,
                                      [],
                                      $("#admin-roleInputModal").val());
            patient.create(update_users);
            $("#admin-patientModal").modal("hide");
        });
    });
});

