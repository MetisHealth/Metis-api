const list_item_calendar = '<li class="patient-item-small list-group-item"></li>'

var appointment = new Appointment(null, null, null, null, null, false, false);
var last_clicked = null;

$(window).ready(function() {
    var popTemplate = [
        '<div class="popover" style="max-width:600px;" >',
        '<div class="arrow"></div>',
        '<div class="popover-title">',
       // '<button id="closepopover" type="button" class="close" aria-hidden="true">&times;</button>',
        '<h3 class="popover-header"></h3>',
        '</div>',
        '<div class="popover-body"></div>',
        '</div>'].join('');

    function closePopovers() {
        $('.popover').not(this).popover('hide');
    }

    var calendarEl = document.getElementById('calendar-container');
    var calendar = new FullCalendar.Calendar(calendarEl, {
		selectMirror: true,
        themeSystem: 'bootstrap',
        initialView: 'timeGridWeek',
        aspectRatio: 1.7,
        firstDay: 1,
        allDaySlot: false,
        scrollTime: "07:00:00",
        locale: "tr",
        selectable: true,
        editable: true,
        headerToolbar: {
            left: 'prev,next,today',
            center: 'title',
            right: 'timeGridDay,timeGridWeek,dayGridMonth',
            height: "100%"

        },
        select: function(info){
            if(calendar.view.type != "timeGridWeek"){
                return
            }
			closePopovers();
            $("#searchResults").toggle(false);
            appointment = new Appointment(null, null, null, null, null, false, false);
            let start = info.start;
            let end = info.end;
            $("#newAppointmentEnd").val(`${(end.getDate() < 10) ? "0" + (end.getDate()).toString() : end.getDate()}/${(end.getMonth() + 1 < 10) ? "0" + (end.getMonth()+1).toString() : end.getMonth() + 1}/${end.getFullYear()}, ${(end.getHours() < 10) ? "0" + end.getHours().toString() : end.getHours()}:${(end.getMinutes() < 10) ? "0" + end.getMinutes().toString(): end.getMinutes()}`);
            $("#newAppointmentStart").val(`${(start.getDate() < 10) ? "0" + (start.getDate()).toString() : start.getDate()}/${(start.getMonth() + 1 < 10) ? "0" + (start.getMonth()+1).toString() : start.getMonth() + 1}/${start.getFullYear()}, ${(start.getHours() < 10) ? "0" + start.getHours().toString() : start.getHours()}:${(start.getMinutes() < 10) ? "0" + start.getMinutes().toString(): start.getMinutes()}`);
            $("#newAppointmentReceipt").prop("checked", false);
            $("#payment-info-container").toggle(false);
			$("#newAppointmentModal").modal("show");
        },
        eventDidMount: function(info){
        },
        eventClick: function(info){
            closePopovers();
            $("#popoverName").html(info.event.title);
            $("#popoverPhone").html(info.event.extendedProps.phone);
            $("#popoverCovid").html(info.event.extendedProps.safe ? "YES": "NO");
            if(!info.event.extendedProps.online){
                $("#popoverUrlSection").addClass("hide");
            }else{
                $("#popoverUrlSection").removeClass("hide");
            }
            if(!info.event.extendedProps.receipt){
                $("#popoverPriceSection").addClass("hide");
            }else{
                $("#popoverPriceSection").removeClass("hide");
            }
            popoverElement = $(info.el);
            popoverEvent = true;
            $(info.el).popover({
                title: "Appointment",
                placement:'left',
                trigger : 'manual',
                sanitize: false,
                template: popTemplate,
                content: function () {
              		return $("#popoverContent").html();
            	},
				html: true,
                container:'#calendar-container-main'
            }).popover("show").on('shown.bs.popover', function () {
                $(".cancelAppointmentPopover").on("click", function(e){
                    $.post("/api/appointments/delete", JSON.stringify(info.event.extendedProps.app_obj), function(data){console.log(data);}, "json");
                    info.event.remove();
                    closePopovers();
                });
            });
        },
        events: function(info, successCallback, failureCallback){
            let url_parameterized = window.location.protocol + "//" + window.location.host + `/api/appointments?start=${encodeURIComponent(info.startStr)}&end=${encodeURIComponent(info.endStr)}`
            $.get(url_parameterized, function(data, status){
				if(status == 500){
					failureCallback("An error occured");
				}else{
					successCallback(
						data.map(function(x) {
						  return {
							title: x.patient.name,
							start: Date.parse(x.start),
							end: Date.parse(x.end),
                            extendedProps: {
                                phone: x.patient.phone,
                                safe: x.patient.covid_safe,
                                online: x.online,
                                receipt: x.receipt,
                                price: x.price,
                                zoom: x.zoom_url,
                                app_obj: x
                            }
						  }
						})
					  );
				}
            });
        }
    });

    var popoverElement;
    var popoverEvent;

    calendar.render();

    $("#newAppointmentPatient").focus(function(){
        $("#searchResults").toggle(true);
    });

    $(document).mousedown(function(e) {
        last_clicked = $(e.target);
    })

    $("#newAppointmentPatient").blur(function(event){
        if(! last_clicked[0].classList.contains("patient-item-small")){
            if(appointment.patient != null){
                $("#newAppointmentPatient").val(appointment.patient.name);
            }else{
                $("#newAppointmentPatient").val("");
            }
            $("#searchResults").toggle(false);
        }
    });

    $(".patient-item-small").on("click",function(e){
        let name = e.target.innerHtml.split("(")[0].trim();
        $("#newAppointmentPatient").val(name);
    });

    $("#newAppointmentPatient").on("input", function(event){
        let url_parameterized =  window.location.protocol + "//" + window.location.host + "/api/patients" + `?email=&phone=&name=${$("#newAppointmentPatient").val()}&page=&psize=`

        $.get(url_parameterized, function(data, status){
            console.log(data);

            $("#searchResults").empty();
            let c = 1;

            if(data.patients.length == 1){
                appointment.patient = Patient.from(data.patients[0]);
            }

            for(var i in data.patients){
                let item = $(list_item_calendar);
                let patient = data.patients[i];
                item.text(patient.name + " (" + patient.phone + ")");
                item.on("click", function(e){
                    appointment.patient = Patient.from(patient);
                    $("#newAppointmentPatient").val(patient.name);
                    $("#searchResults").toggle(false);
                });
                item.appendTo($("#searchResults"));
                if(c == 10){
                    break;
                }
                c++;
            };
        });
    });

	$('body').on('click', function (e) {
		if (popoverElement && ((!popoverElement.is(e.target) && popoverElement.has(e.target).length === 0 && $('.popover').has(e.target).length === 0) || (popoverElement.has(e.target) && e.target.id === 'closepopover')) && popoverEvent) {
			closePopovers();
		}
	});

    $("#newAppointmentReceipt").click(function(){
        appointment.receipt = $(this).is(":checked");
        $("#payment-info-container").toggle($(this).is(":checked"));
    });

    $("#newAppointmentModal .save-button").on("click", function(e){

        if(appointment.receipt){
            appointment.price = $("newAppointmentPrice").val();
            if(appointment.price == null){

            }
        }else{
            delete appointment.price;
        }

        appointment.online = $("#newAppointmentOnline").is(":checked");
        appointment.start = $("#newAppointmentStart").val();
        appointment.end = $("#newAppointmentEnd").val();
        delete appointment.id;
        $.post("/api/appointments", JSON.stringify(appointment), function(data){
             calendar.addEvent({
                start: Date.parse(appointment.start),
                end: Date.parse(appointment.end),
                title: appointment.patient.name,
                extendedProps: {
                    phone: appointment.patient.phone,
                    safe: appointment.patient.covid_safe,
                    online: appointment.online,
                    receipt: appointment.receipt,
                    price: appointment.price,
                    zoom: appointment.zoom_url,
                    app_obj: data.appointment
                }
            });
            console.log(data);
        }, "json");

        $("#newAppointmentModal").modal("hide");
    });
});
