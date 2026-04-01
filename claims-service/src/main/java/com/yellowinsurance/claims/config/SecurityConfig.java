package com.yellowinsurance.claims.config;

import com.yellowinsurance.claims.ClaimsApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * VULNERABILITIES IN THIS CLASS:
 * - CSRF disabled globally
 * - CORS allows all origins
 * - Using NoOpPasswordEncoder (passwords stored in plain text)
 * - Hardcoded credentials
 * - H2 console exposed without restriction
 * - Some endpoints not protected
 */
@Configuration
@EnableWebSecurity
@SuppressWarnings("deprecation")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // VULNERABILITY: CSRF disabled - enables cross-site request forgery
            .csrf(csrf -> csrf.disable())
            // VULNERABILITY: Permissive CORS
            .cors(cors -> {})
            // VULNERABILITY: H2 console accessible - allows direct DB manipulation
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .authorizeHttpRequests(auth -> auth
                // VULNERABILITY: Health and system-info endpoints are public
                .requestMatchers("/api/v1/admin/health").permitAll()
                .requestMatchers("/api/v1/admin/system-info").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                // ISSUE: All other endpoints only require basic auth - no role-based access
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> {});
        return http.build();
    }

    // VULNERABILITY: Hardcoded credentials with no role separation
    @Bean
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername(ClaimsApplication.ADMIN_USERNAME)
            .password(ClaimsApplication.ADMIN_PASSWORD)
            .roles("USER", "ADMIN")
            .build());
        manager.createUser(User.withUsername("adjuster")
            .password("adjust123")
            .roles("USER")
            .build());
        manager.createUser(User.withUsername("viewer")
            .password("view123")
            .roles("USER")
            .build());
        return manager;
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
