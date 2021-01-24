package net.metisapp.metisapi.config;

import net.metisapp.metisapi.entities.MetisUser;
import net.metisapp.metisapi.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException; 
import org.springframework.beans.factory.annotation.Autowired;

public class ClinicUserDetailsService implements UserDetailsService{ 

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String uname){
        MetisUser user = userRepository.findByEmail(uname);

        if(user == null){
            throw new UsernameNotFoundException("Could not find user!");
        }
        return user;
    }

}
