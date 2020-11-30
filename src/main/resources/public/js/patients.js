const URL = window.location.protocol + "//" + window.location.host + "/api/patients";

function update_patients(text){

    let url_parameterized = URL + `?email=${$("#mail-input").val()}&\
phone=${$("#phone-input").val()}&name=${$("#name-input").val()}`
    $.get(url_parameterized, function(data, status){
        console.log(data);
    });

}

update_patients("");

$("#name-input").on('input', update_patients);
$("#phone-input").on('input', update_patients);
$("#mail-input").on('input', update_patients);
