package com.yigitcolakoglu.Metis;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;

@Controller
public class WebController {

    @Autowired
    private UserRepository userRepository;
        
    @RequestMapping("/doctor")
	public String doctor(Model model, Authentication auth) {
        model.addAttribute("display_api", true);
        model.addAttribute("name", userRepository.findByEmail(auth.getName()).getName());
		return "doctor";
	}

    @RequestMapping("/admin")
	public String admin(Model model, Authentication auth) {
        model.addAttribute("display_api", true);
        model.addAttribute("name", userRepository.findByEmail(auth.getName()).getName());
		return "admin";
	}

    @RequestMapping("/")
    public String index(Model model, Authentication auth){
        if(auth == null){
            return "redirect:/login";
        }
        if(!auth.isAuthenticated()){
            return "redirect:/login";
        }

        model.addAttribute("display_api", true);
        model.addAttribute("name", userRepository.findByEmail(auth.getName()).getName());
        if(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("DOCTOR"))){
            return "doctor";         
        }else if(auth.getAuthorities().stream().anyMatch(a-> a.getAuthority().equals("ADMIN"))){
            return "admin";
        }else if(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("PATIENT"))){
            return "patient";
        }    
        return null;
    }
}
