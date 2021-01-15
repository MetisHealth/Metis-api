package com.yigitcolakoglu.Metis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{
    @Value("${secret.appkey}")
    public String APP_KEY;

    @Autowired
    private DataSource dataSource;

    @Bean
    public AuthenticationSuccessHandler myAuthenticationSuccessHandler(){
        return new SimpleAuthenticationSuccessHandler();       
    }
    @Bean 
    public UserDetailsService userDetailsService(){
        return new ClinicUserDetailsService();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public RememberMeAuthenticationProvider rememberMeAuthenticationProvider(){
        RememberMeAuthenticationProvider provider = new RememberMeAuthenticationProvider(APP_KEY);
        return provider;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl db = new JdbcTokenRepositoryImpl();
        //db.setCreateTableOnStartup(true);
        db.setDataSource(dataSource);
        return db;
    } 

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests((authorize) -> authorize
                    .antMatchers("/public/**").permitAll()
                    .anyRequest().authenticated()
			)
			.formLogin((formLogin) -> formLogin
					.permitAll()
                    .successHandler(this.myAuthenticationSuccessHandler())
            )
            .rememberMe((rememberMe) -> rememberMe 
                    .key(APP_KEY)
                    .userDetailsService(this.userDetailsService())
                    .tokenRepository(this.persistentTokenRepository())
            );
	}
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(this.userDetailsService());
        auth.authenticationProvider(authenticationProvider());
        auth.authenticationProvider(this.rememberMeAuthenticationProvider());
    }


} 
