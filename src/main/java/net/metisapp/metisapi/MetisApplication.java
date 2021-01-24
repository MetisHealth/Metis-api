package net.metisapp.metisapi;

import io.sentry.Sentry;
import net.metisapp.metisapi.config.MetisUserDetailsService;
import org.springframework.boot.SpringApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class MetisApplication{

    public static void main(String[] args) {
		SpringApplication.run(MetisApplication.class, args);
	}

	@Bean
	public MetisUserDetailsService userDetailsService(){
		return new MetisUserDetailsService();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
