package com.yellowinsurance.claims.config;

import com.yellowinsurance.claims.ClaimsApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * VULNERABILITIES IN THIS CLASS:
 * - CSRF disabled globally
 * - CORS allows all origins
 * - Using deprecated WebSecurityConfigurerAdapter
 * - Using NoOpPasswordEncoder (passwords stored in plain text)
 * - Hardcoded credentials
 * - H2 console exposed without restriction
 * - Some endpoints not protected
 */
@Configuration
@EnableWebSecurity
@SuppressWarnings("deprecation")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // VULNERABILITY: CSRF disabled - enables cross-site request forgery
            .csrf().disable()
            // VULNERABILITY: Permissive CORS
            .cors().and()
            // VULNERABILITY: H2 console accessible - allows direct DB manipulation
            .headers().frameOptions().disable()
            .and()
            .authorizeRequests()
                // VULNERABILITY: Health and system-info endpoints are public
                .antMatchers("/api/v1/admin/health").permitAll()
                .antMatchers("/api/v1/admin/system-info").permitAll()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                // ISSUE: All other endpoints only require basic auth - no role-based access
                .anyRequest().authenticated()
            .and()
            .httpBasic();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // VULNERABILITY: Hardcoded credentials with no role separation
        auth.inMemoryAuthentication()
            .withUser(ClaimsApplication.ADMIN_USERNAME)
            .password(ClaimsApplication.ADMIN_PASSWORD)
            .roles("USER", "ADMIN")
            .and()
            .withUser("adjuster")
            .password("adjust123")
            .roles("USER")
            .and()
            .withUser("viewer")
            .password("view123")
            .roles("USER");
    }

    // VULNERABILITY: NoOpPasswordEncoder - passwords not hashed
    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // VULNERABILITY: Allow all origins
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        // VULNERABILITY: Allow credentials with wildcard origin
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
