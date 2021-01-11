const disabled_URL = window.location.protocol + "//" + window.location.host + "/api/disabled";
var page_admin = 1;
const disabled_item = '\
		  <a href="#" class="disabled-rule-item list-group-item list-group-item-action flex-column align-items-start">\
			<div class="d-flex w-100 justify-content-between">\
              <span>\
                <input style="width:80%;" class="disabled-name text-input" type="text" placeholder="Name" >\
              </span>\
              <span>\
                <label>Start:</label> <input style="width: 70%;" class="disabled-start text-input" type="text" placeholder="0000">\
              </span>\
              <span>\
                <label>Duration: </label> <input class="disabled-duration text-input" type="text" placeholder="0000">\
              </span>\
              <span>\
                <label>Interval: </label> <input class="disabled-interval text-input" type="text" placeholder="0000">\
              </span>\
              <div style="width:10%; height:90%;">\
                <button type="button" class="hidden disabled-button save-disabled p-2 bd-highlight btn btn-success"><i class="fa fa-check" aria-hidden="true"></i></button>\
                <button type="button" class="disabled-button delete-disabled p-2 bd-highlight btn btn-danger"><i class="fa fa-trash" aria-hidden="true"></i></button>\
              </div>\
			</div>\
		  </a>'


$.ajaxSetup({
      contentType: "application/json; charset=utf-8"
});

function update_rules(text){

    $.get(disabled_URL + "/rules", function(data, status){
        $(".disabled-list").empty();
        let item = $(disabled_item);
        item.attr("id", `disabled-rule-init`)
        item.remove(".delete-disabled");
        item.find(".text-input").on("input", function(e){
            e.stopPropagation();
            $(`#disabled-rule-init .save-disabled`).toggle(true);
        });
        item.find(".save-disabled").on("click", function(e){
            e.stopPropagation();
            $.post(disabled_URL, JSON.stringify({
                "name": $(`#disabled-rule-init .disabled-name`).val(),
                "start": $(`#disabled-rule-init .disabled-start`).val(),
                "duration": parseInt(parseInt($(`#disabled-rule-init .disabled-duration`).val()) * 1000*60*60),
                "repetition": parseInt(parseInt($(`#disabled-rule-init .disabled-interval`).val()) * 1000*60*60),
            }), function(data){
                },"json");
            $(`#disabled-rule-init .save-disabled`).toggle(false);
        });
        item.appendTo($(".disabled-list"));
        data.forEach(function(rule){
            let item = $(disabled_item);
            item.attr("id", `disabled-rule-${rule.id}`)
            item.find(".disabled-name").val(rule.name);
            item.find(".disabled-start").val(rule.start);
            item.find(".disabled-duration").val((rule.duration / (1000*60*60)).toFixed(2));
            item.find(".disabled-interval").val((rule.repetition / (1000*60*60)).toFixed(2));
            item.find(".text-input").on("input", function(e){
                e.stopPropagation();
                $(`#disabled-rule-${rule.id} .save-disabled`).toggle(true);
            });
            item.find(".delete-disabled").on("click", function(e){
                e.stopPropagation();
                $.get(disabled_URL + `/delete?id=${rule.id}`,function(data){
                    console.log(data);
                })
                $(`#disabled-rule-${rule.id}`).remove();
            });
            item.find(".save-disabled").on("click", function(e){
                e.stopPropagation();
                $.post(disabled_URL + `/update`, JSON.stringify({
                    "id": rule.id,
                    "name": $(`#disabled-rule-${rule.id} .disabled-name`).val(),
                    "start": $(`#disabled-rule-${rule.id} .disabled-start`).val(),
                    "duration": parseInt(parseInt($(`#disabled-rule-${rule.id} .disabled-duration`).val()) * 1000*60*60),
                    "repetition": parseInt(parseInt($(`#disabled-rule-${rule.id} .disabled-interval`).val()) * 1000*60*60),
                }), function(data){
                },"json");
                $(`#disabled-rule-${rule.id} .save-disabled`).toggle(false);
            });
            item.appendTo($(".disabled-list"));
        });
    });

}
$(document).ready(function(){
    update_rules("");
});

