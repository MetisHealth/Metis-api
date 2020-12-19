package com.yigitcolakoglu.Metis;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Arrays;


public class ClinicUserDetails implements UserDetails{

    private final User user;

    public ClinicUserDetails(User user){
        this.user = user;
    }

    @Override
    public String getPassword(){
        return user.getPassword();
    }

    @Override
    public String getUsername(){
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired(){
        return true;
    }

    @Override
    public boolean isAccountNonLocked(){
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired(){
        return true;
    }

    @Override
    public boolean isEnabled(){
        return true;
    }

    @Override 
    public Collection<GrantedAuthority> getAuthorities(){
       SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());
       return Arrays.asList(authority);
    }
}
