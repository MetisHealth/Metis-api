$(document).ready(function(){
    $.get("/api/profile", function(data, status){

        window.Metis.profile = data;
        $("#profile-name").val(data.name);
        $("#profile-email").val(data.email);
        $("#profile-hes-code").val(data.hescode);
        $("#profile-phone").val(data.phone);
        $("#profile-tc").val(data.tcno);
        $("#profile-whereby-url").val(data.wherebyUrl)
        if(data.locale){
            $("#profile-locale").val(data.locale);
        }
        $("#profile-password-save").on("click", function(){
            if($("profile-password-new").val() != $("profile-password-new-confirm").val()){
                console.log("Passwords do not match!");
                return
            }
            $.ajax({
                contentType: 'text/plain',
                data: `newPassword=${$("#profile-password-new").val()}&oldPassword=${$("#profile-password-current").val()}`,
                dataType: 'form-data',
                success: function(data){
                    console.log(data);
                },
                error: function(){
                    console.log("an error occured");
                },
                processData: false,
                type: 'POST',
                url: "/api/password"
            });
        });
        $("#profile-hes-send-sms").on("click", function(){
            $.get("/api/hes/sendsms", function(data){
                if(data.code == 200){
                    toastr.info("Sent SMS to your device.");
                }else{
                    toastr.error(data.message);
                }
            });
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
            window.Metis.profile = profile_data;
            $.post("/api/profile/update", JSON.stringify(profile_data), function(data){
                if(data.code == 200){
                    toastr.success("Profile updated successfully.");
                }else{
                    toastr.error(data.message);
                }
            }, "json");
        });
        $("#profile-hes-save").on("click", function(){
            $.get(`/api/hes/smscode?code=${$("#profile-hes-sms-code").val()}`, function(data){
                if(data.code == 200){
                    toastr.success("Authenticated successfully.");
                }else{
                    toastr.error(data.message);
                }
            });
        });
        $("#profile-whereby-save").on("click",function(){
            window.Metis.wherebyUrl = $("#profile-whereby-url").val();
            $.post("/api/whereby/url",$("#profile-whereby-url").val());
        });
    });
});
