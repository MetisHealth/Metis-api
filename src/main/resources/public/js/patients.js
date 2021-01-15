var URL = window.location.protocol + "//" + window.location.host + "/api/patients";
var page = 1;
var tagContainer, input;
var tags = [];

const pagination_prev = '<li class="page-item page-item-patient"><a class="page-link" href="#" aria-label="Previous"><span aria-hidden="true">&laquo;</span></a></li>'
const pagination_next = '<li class="page-item page-item-patient"><a class="page-link" href="#" aria-label="Next"><span aria-hidden="true">&raquo;</span></a></li>'
const list_item = '\
		  <a href="#" class="list-group-item list-group-item-action flex-column align-items-start">\
			<div class="d-flex w-100 justify-content-between">\
              <div class="mr-auto">\
			    <h5 class="mb-1 mr-auto item-name"></h5>\
			    <div class="mb-1"><span class="item-phone"></span></div>\
		        <small class="item-email"></small>\
              </div>\
              <button type="button" class="patient-button safe-patient p-2 bd-highlight btn"><i class="fas fa-biohazard" aria-hidden="true"></i></button>\
              <button type="button" class="patient-button password-patient p-2 bd-highlight btn btn-primary"><i class="fa fa-key" aria-hidden="true"></i></button>\
              <button type="button" class="patient-button delete-patient p-2 bd-highlight btn btn-danger"><i class="fa fa-trash" aria-hidden="true"></i></button>\
			</div>\
		  </a>'

function update_patients(text){

    let url_parameterized = URL + `?email=${$("#mail-input").val()}&\
phone=${$("#phone-input").val()}&name=${$("#name-input").val()}&page=${page - 1}&psize=${$("#page-size-input").val()}&role=PATIENT`

    $.get(url_parameterized, function(data, status){
        console.log(data);
        let page_num = Math.ceil(data.patient_num / parseInt($("#page-size-input").val()));
        if (page_num < page){
            page = page_num;
            update_patients("");
        }
        if ( page_num != $(".page-item-patient")/2 - 2 ){
            let foo = 1;

            $(".pagination-patient").empty();
            let prev = $(pagination_prev);
            if(page == 1){
                prev.addClass("disabled");
            }
            prev.appendTo($(".pagination-patient"));
            let item = null;

            do{
                item = $(`<li class="page-item page-item-patient"><a class="page-link" href="#">${foo}</li>`)

                if(page == foo){
                    item = item.addClass("active");
                }

                item.appendTo($(".pagination-patient"))
                foo++;

            } while ( foo <= page_num );

            let next = $(pagination_next);
            if(page == page_num){
                next.addClass("disabled");
            }
            next.appendTo($(".pagination-patient"));

            $(".page-item-patient").on("click", function(event){
                let foo = event.target.innerText;
                if( foo == "«" ){
                    page--;
                }else if( foo == "»" ){
                    page++;
                }else{
                   page = parseInt(foo);
                }
                console.log(page);
                update_patients("");
            });
        }

        if (page != parseInt($(".page-item-patient.active").first().text())){
            $(".page-item-patient.active").removeClass("active");

            $(".page-item-patient").eq(page).addClass("active");
            $(".page-item-patient").eq(page + page_num + 2).addClass("active");
        }
        $(".patient-list").empty();
        data.patients.forEach(function(patient){
            let item = $(list_item);
            item.attr("id", `patient-${patient.id}`)
            item.find(".item-name").text(patient.name);
            item.find(".item-phone").text(patient.phone);
            item.find(".item-email").text(patient.email);

            if(patient.safe == false){
                item.find(".safe-patient").addClass("btn-warning");
                item.find(".safe-patient").css("color","black");
            }else{
                item.find(".safe-patient").addClass("btn-success");
            }
            item.find(".safe-patient").on("click", function(e){
                e.stopPropagation();
                $.get("/api/hes/check?id="+patient.id, function(data, status){
                    if(data.code != 200){
                        toastr.error(data.message);
                        return;
                    }
                    if(data.message == "safe"){
                        toastr.success(`${patient.name} is safe from COVID.`)
                        $(`#patient-${patient.id} .safe-patient`).removeClass("btn-warning");
                        $(`#patient-${patient.id} .safe-patient`).addClass("btn-success");
                        $(`#patient-${patient.id} .safe-patient`).css("color", "");
                    }else{
                        toastr.warning(`${patient.name} is not safe from COVID.`)
                        $(`#patient-${patient.id} .safe-patient`).removeClass("btn-success");
                        $(`#patient-${patient.id} .safe-patient`).addClass("btn-warning");
                        $(`#patient-${patient.id} .safe-patient`).css("color", "black");
                    }
                })
            });
            item.find(".delete-patient").on("click", function(e){
                e.stopPropagation();
                Patient.from(patient).delete(window.calendar.render);
                $(`#patient-${patient.id}`).remove();
            });
            item.find(".password-patient").on("click", function(e){
                e.stopPropagation(); // TODO implement actual password change
                $("#passwordModal").modal("show");
                $("#passwordModalSave").on("click", function(e){
                    let password_change_url = window.location.protocol + "//" + window.location.host + "/api/patient/password";
					$.ajax({
						contentType: 'text/plain',
						data: `newPassword=${$("#passwordModalPassword").val()}&email=${patient.email}`,
						dataType: 'form-data',
						success: function(data){
                            toastr.success("Password changed successfully!");
						},
						error: function(){
                            toastr.error("An error occured");
						},
						processData: false,
						type: 'POST',
						url: password_change_url
					});
                });
            });
            item.on("click", function(event){
                tags = patient.protocolNumbers;
                if(!tags){
                    tags = [];
                }
                addTags();
                $("#patientModalTitle").text("Edit Patient");
                $("#nameInputModal").val(patient.name);
                $("#emailInputModal").val(patient.email);
                $("#phoneInputModal").val(patient.phone);
                $("#hesInputModal").val(patient.hescode);
                $("#tcInputModal").val(patient.tcno);
                $("#patientModal").modal("show");
                $("#patientModalSave").unbind(); // Clear events for save button
                $("#patientModalSave").on("click", function(event){
                    let new_patient = new Patient(patient.id,
                                              $("#nameInputModal").val(),
                                              $("#phoneInputModal").val(),
                                              $("#emailInputModal").val(),
                                              $("#tcInputModal").val(),
                                              $("#hesInputModal").val(),
                                              1,
                                              tags,
                                              "PATIENT");
                    new_patient.update(update_patients);
                    $("#patientModal").modal("hide");
                });
            });
            item.appendTo($(".patient-list"));
        });
    });

}


function createTag(pnum) {
  let label = pnum.number.toString();
  const div = document.createElement('div');
  div.setAttribute('class', 'tag');
  div.setAttribute("data-toggle", "tooltip");
  div.setAttribute("data-placement", "top");
  div.setAttribute("title",new Date(pnum.addedDate).toLocaleDateString());
  const span = document.createElement('span');
  span.innerHTML = label;
  const closeIcon = document.createElement('i');
  closeIcon.setAttribute('class', 'fas fa-times close-tag');
  closeIcon.setAttribute('data-item', label);
  div.appendChild(span);
  div.appendChild(closeIcon);
  return div;
}

function clearTags() {
  document.querySelectorAll('.tag').forEach(tag => {
    tag.parentElement.removeChild(tag);
  });
}

function addTags() {
  clearTags();
  tags.slice().reverse().forEach(tag => {
    tagContainer.prepend(createTag(tag));
  });
}

$(document).ready(function(){
    update_patients("");
    tagContainer = document.querySelector('.patient-tag-container');
    input = document.querySelector('.patient-tag-container input');


    $("#name-input").on('input', update_patients);
    $("#phone-input").on('input', update_patients);
    $("#mail-input").on('input', update_patients);
    $("#page-size-input").on('input', update_patients);
    $("#newPatient").on("click", function(event){ // TODO Check for e-mail collisions
        clearTags();
        $("#patientModalSave").unbind(); // Clear events for save button
        $("#patientModalTitle").text("New Patient");
        $("#patientModal input").val("");
        $("#patientModalSave").on("click", function(event){
            let patient = new Patient(null,
                                      $("#nameInputModal").val(),
                                      $("#phoneInputModal").val(),
                                      $("#emailInputModal").val(),
                                      $("#tcInputModal").val(),
                                      $("#hesInputModal").val(),
                                      1,
                                      [],
                                      "PATIENT");
            patient.create(update_patients);
            $("#patientModal").modal("hide");
        });
    });
    $("#passwordModalGenerate").on("click", function(e){
		var pwdChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		var pwdLen = 12;
		var randPassword = Array(pwdLen).fill(pwdChars).map(function(x) { return x[Math.floor(Math.random() * x.length)] }).join('');
		$("#passwordModalPassword").val(randPassword);
    });
    input.addEventListener('keyup', (e) => {
        if (e.key === 'Enter') {
          e.target.value.split(',').forEach(tag => {
            if(isNaN(tag)){
                return;
            }
            tags.push({
                number : parseInt(tag),
                addedDate: new Date().toISOString()
            });
          });

          addTags();
          input.value = '';
        }
    });

    document.addEventListener('click', (e) => {
      if (e.target.classList.contains("close-tag")) {
        const tagLabel = e.target.getAttribute('data-item');
        let index;
        for(let i=0; i < tags.length; i++){
            if(parseInt(tagLabel) == tags[i].number){
                index = i;
                break;
            }
        }
        tags = [...tags.slice(0, index), ...tags.slice(index+1)];
        addTags();
      }
    })

});

