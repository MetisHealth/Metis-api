const list_item_calendar = '<li class="patient-item-small list-group-item"></li>'

var appointment = new Appointment(null, null, null, null, false, false);
var last_clicked = null;

function waitForElement(){
    if(typeof window.Metis.profile!== "undefined"){
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

    while(!window.Metis.profile){
        console.log(window.Metis.profile);
        continue;
    }

    var calendarEl = document.getElementById('calendar-container');
    window.calendar = new FullCalendar.Calendar(calendarEl, {
		selectMirror: true,
        themeSystem: 'bootstrap',
        initialView: 'timeGridWeek',
        eventOverlap: false,
        selectOverlap: false,
        aspectRatio: 1.7,
        firstDay: 1,
        allDaySlot: false,
        scrollTime: "07:00:00",
        locale: window.Metis.profile.locale,
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
			$("#newAppointmentModal").modal("show");
        },
        eventDidMount: function(info){
        },
        eventClick: function(info){
            closePopovers();
            if(info.event.display == 'background'){
                return;
            }
            $("#popoverName").text(info.event.title);
            $("#popoverPhone").text(info.event.extendedProps.phone);
            $("#popoverCovid").text(info.event.extendedProps.safe ? "NO": "YES");
            $("#popoverUrl").html(`<a target="_blank" rel="noopener noreferrer" href="${window.Metis.profile.wherebyUrl.replaceAll("\"","\\\"")}">Link</a>`);
            if(!info.event.extendedProps.online){
                $("#popoverUrlSection").addClass("hide");
            }else{
                $("#popoverUrlSection").removeClass("hide");
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
                    $.post("/api/appointments/delete", JSON.stringify(info.event.extendedProps.app_obj), function(data){
                        if(data.code == 200){
                            toastr.success("Appointment deleted successfully.");
                        }else{
                            toastr.error(data.message);
                        }
                    }, "json");
                    info.event.remove();
                    closePopovers();
                });
            });
        },
        events: function(info, successCallback, failureCallback){
            let url_parameterized = window.location.protocol + "//" + window.location.host + `/api/appointments?start=${encodeURIComponent(info.startStr)}&end=${encodeURIComponent(info.endStr)}`
            $.get(url_parameterized, function(data, status){
				if(status == 500){
                    toastr.error("An error occured");
					failureCallback("An error occured");
				}else{
                    let disabled_url_parameterized = window.location.protocol + "//" + window.location.host + `/api/disabled?start=${encodeURIComponent(info.startStr)}&end=${encodeURIComponent(info.endStr)}`
                    $.get(disabled_url_parameterized, function(disabled_data, disabled_status){
                        if(disabled_status == 500){
                            toastr.error("An error occured");
					        failureCallback("An error occured");
				        }else{
                            appointment_arr = data.map(function(x) {
                              return {
                                title: x.patient.name,
                                start: Date.parse(x.start),
                                end: Date.parse(x.end),
                                backgroundColor: x.patient.safe ? "#1266F1" : "#FF9100",
                                extendedProps: {
                                    phone: x.patient.phone,
                                    safe: x.patient.safe,
                                    online: x.online,
                                    zoom: x.zoom_url,
                                    app_obj: x
                                }
                              }
                            });
                            for(var n in disabled_data){
                                appointment_arr = appointment_arr.concat(disabled_data[n].map(function(x){
                                    return {
                                        title: n,
                                        start: x[0],
                                        end: x[1],
                                        display: 'background',
                                        backgroundColor: '#757575',
                                        foregroundColor: "#000000"
                                    }
                                }));
                            }
					        successCallback(appointment_arr);
                        }
                    });
				}
            });
        }
    });

    var popoverElement;
    var popoverEvent;
    calendar.render();

    Metis.calendar = calendar;
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
        let url_parameterized =  window.location.protocol + "//" + window.location.host + "/api/patients" + `?role=PATIENT&email=&phone=&name=${$("#newAppointmentPatient").val()}&page=&psize=`

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

    $("#newAppointmentModal .save-button").on("click", function(e){

        appointment.online = $("#newAppointmentOnline").is(":checked");
        appointment.start = $("#newAppointmentStart").val();
        appointment.end = $("#newAppointmentEnd").val();
        delete appointment.id;
        $.post("/api/appointments", JSON.stringify(appointment), function(data){
             calendar.addEvent({
                start: Date.parse(appointment.start),
                end: Date.parse(appointment.end),
                title: appointment.patient.name,
                backgroundColor: appointment.patient.safe ? "#1266F1" : "#FF9100",
                extendedProps: {
                    phone: appointment.patient.phone,
                    safe: appointment.patient.safe,
                    online: appointment.online,
                    price: appointment.price,
                    zoom: appointment.zoom_url,
                    app_obj: data.appointment
                }
            });
            if(data.code == 200){
                toastr.success("Appointment created successfully.");
            }else{
                toastr.error(data.message);
            }
        }, "json");

        $("#newAppointmentModal").modal("hide");
    });
}else{
        setTimeout(waitForElement, 250);
    }
}

waitForElement()
