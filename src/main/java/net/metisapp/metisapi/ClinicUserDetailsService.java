package net.metisapp.metisapi;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException; 
import org.springframework.beans.factory.annotation.Autowired;

public class ClinicUserDetailsService implements UserDetailsService{ 

    @Autowired
    private UserRepository userRepository;
//    @Override 
//    public boolean userExists(String uname){ 
//       return false;  
//    }
 
//    @Override 
//    public void changePassword(String prev_pass, String new_pass){
//    
//    } 
   
//    @Override 
//    public void deleteUser(String uname){
 
//    } 
//     
//    @Override 
//    public void updateUser(UserDetails user){
 
//    } 
 
//    @Override 
//    public void createUser(UserDetails user){
 
//    } 
//     
    @Override
    public UserDetails loadUserByUsername(String uname){
        User user = userRepository.findByEmail(uname);

        if(user == null){
            throw new UsernameNotFoundException("Could not find user!");
        }
        return new ClinicUserDetails(user);  
    }

}
