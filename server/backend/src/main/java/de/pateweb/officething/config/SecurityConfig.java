package de.pateweb.officething.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import de.pateweb.officething.auth.MyUserDetailService;

/**
 * Configure the Basic Authentication in @link{Security.java}
 *
 * @author Octavian Pate
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	MyUserDetailService userDetailsService;

	@Override
	protected void configure(AuthenticationManagerBuilder auth)
	  throws Exception {
	    auth.authenticationProvider(authenticationProvider());
	}
	 
    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        http
        .cors()
        .and()
        .csrf().disable()
        .authorizeRequests()
	     .anyRequest().authenticated()
	     .and()
	     .httpBasic();
    }	
	
    
    // attention. The previous .cors() and this method may be for develop reasons only
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
    	
    	CorsConfiguration corsConfig = new CorsConfiguration();
    	corsConfig.setAllowedHeaders(Collections.unmodifiableList(
			Collections.singletonList("*")));
    	corsConfig.setAllowedOrigins(Collections.unmodifiableList(
			Collections.singletonList("*")));
    	corsConfig.setAllowedMethods(Collections.unmodifiableList(
			Arrays.asList(HttpMethod.GET.name(), 
					HttpMethod.HEAD.name(), 
					HttpMethod.POST.name(),
					HttpMethod.OPTIONS.name(),
					HttpMethod.PUT.name(),
					HttpMethod.DELETE.name())));
    	corsConfig.setMaxAge(1800L);
    	
    	
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/ui/workinghours/**", corsConfig);
        return source;         
    }  
    
    
    
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
	    DaoAuthenticationProvider authProvider
	      = new DaoAuthenticationProvider();
	    authProvider.setUserDetailsService(userDetailsService);
	    authProvider.setPasswordEncoder(encoder());
	    return authProvider;
	}
	 
	@Bean
	public PasswordEncoder encoder() {
	    return new BCryptPasswordEncoder(11);
	}
	 	
}
