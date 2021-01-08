

window.Metis = {};

$.ajaxSetup({
      contentType: "application/json; charset=utf-8"
});

$(document).ready(function () {

    $("#menu-toggle").click(function(e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });

    var active_id;
    if(window.location.hash != "" && window.location.hash != "calendar-container"){
        active_id = window.location.hash;
        $("#calendar-container").toggle();
        $(active_id).toggle();
    }else{
        active_id = "#calendar-container";
        window.location.hash = "calendar-container"
    }

    $(".sidebar-item").on("click", function(e){
        let foo = e.currentTarget.attributes.target.value;
        if(foo != active_id){
            $(active_id).toggle();
            $(foo).toggle();
            active_id = foo;
            window.location.hash = active_id.substr(1);
        }
    });
});
