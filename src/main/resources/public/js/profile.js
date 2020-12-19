$(document).ready(function(){
    $.get("/api/profile", function(data, status){
        console.log(data);
        $("#profile-name").val(data.name);
        $("#profile-email").val(data.email);
        $("#profile-hes-code-group").toggle(false);
        $("#profile-hes-code").val(data.hescode);
        $("#profile-phone").val(data.phone);
        $("#profile-tc").val(data.tcno);
        if(data.locale){
            $("#profile-locale").val(data.locale);
        }
        $("#profile-hes-send-sms").on("click", function(){
            $("#profile-hes-send-sms").toggle(false);
            $("#profile-hes-code-group").toggle(true);
            $.get("/api/hes/sendsms");
        });
        $("#profile-save").on("click", function(){
            let profile_data = {
                "name": $("#profile-name").val(),
                "email": $("#profile-email").val(),
                "hescode": $("#profile-hes-code").val(),
                "phone": $("#profile-phone").val(),
                "tcno": $("#profile-tc").val(),
                "locale": $("#profile-locale").val()
            };
            $.post("/api/profile/update", JSON.stringify(profile_data), function(data){
                console.log(data);
            }, "json");
        });
        $("#profile-hes-save").on("click", function(){
            $("#profile-hes-send-sms").toggle(true);
            $("#profile-hes-code-group").toggle(false);
            $.get(`/api/hes/smscode?code=${$("#profile-hes-sms-code").val()}`);
        });
    });
});
