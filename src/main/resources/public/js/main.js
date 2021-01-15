

window.Metis = {};

var token = $("meta[name='_csrf']").attr("content");
var header = $("meta[name='_csrf_header']").attr("content");

toastr.options.closeButton = true;
toastr.options.escapeHtml = true;

$(document).ajaxSend(function(e, xhr, options) {
    xhr.setRequestHeader(header, token);
});

$.ajaxSetup({
      contentType: "application/json; charset=utf-8",
      error: AjaxError
});

function AjaxError(x, e) {
  if (x.status == 0) {
      toastr.warning("Check your network!");
  }  else {
      toastr.error("An unknown error occured!");
  }
}

$(document).ready(function () {
    $(function () {
      $('[data-toggle="tooltip"]').tooltip()
    })
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
            if(foo == "#calendar-container"){
                window.calendar.render();
            }
        }
    });
});
