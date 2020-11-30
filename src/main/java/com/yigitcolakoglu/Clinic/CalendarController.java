package com.yigitcolakoglu.Clinic;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CalendarController {

    @RequestMapping({"/","/calendar"})
	public String calendar(Model model,HttpServletRequest request) {
        //model.addAttribute("name", name);
        System.out.println(request.getAttribute("username"));
        System.out.println(request.getAttribute("id"));
		return "calendar";
	}

}
