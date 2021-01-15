package com.yigitcolakoglu.Metis;

import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.boot.web.servlet.error.ErrorController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.RequestDispatcher;

@Controller
public class WebController implements ErrorController{
    
    private static final String ERROR_PATH = "/error"; 
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

        model.addAttribute("name", userRepository.findByEmail(auth.getName()).getName());
        if(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("DOCTOR"))){
            model.addAttribute("display_api", true);
            return "doctor";         
        }else if(auth.getAuthorities().stream().anyMatch(a-> a.getAuthority().equals("ADMIN"))){
            model.addAttribute("display_api", true);
            return "admin";
        }else if(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("PATIENT"))){
            model.addAttribute("display_api", false);
            return "patient";
            
        return null;
    }

    @RequestMapping(ERROR_PATH)
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
        
            if(statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error-404";
            }
            else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error-505";
            }
        }
        return "error-505";
    }

    @Override
    public String getErrorPath() { // This method is deprecated but I don't think there is another way of achieving this right now. 
        return ERROR_PATH;
    }

}
