document.addEventListener('DOMContentLoaded', function() {
var calendarEl = document.getElementById('calendar');
var calendar = new FullCalendar.Calendar(calendarEl, {
    timeZone: 'GMT+3',
    themeSystem: 'bootstrap',
    initialView: 'timeGridWeek',
    headerToolbar: {
        left: 'prev, next today',
        center: 'title',
        right: 'timeGridDay,timeGridWeek,dayGridMonth',
        height: "100%"

    },
    events: 'https://fullcalendar.io/demo-events.json'
});
calendar.render();
});
