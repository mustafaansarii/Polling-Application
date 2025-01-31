package com.example.polls.config;

import com.example.polls.security.CustomUserDetailsService;
import com.example.polls.security.JwtAuthenticationEntryPoint;
import com.example.polls.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtAuthenticationEntryPoint unauthorizedHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
    }

    // Bean for JWT Authentication filter
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    // Password encoder to hash passwords (bcrypt in this case)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Authentication manager that uses custom user details service and password encoder
    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }

    // CORS configuration - allows specific origins, methods, and headers
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:3000");  // Add your allowed origins here (production ready)
        configuration.addAllowedMethod("*"); // Allows all methods (GET, POST, etc.)
        configuration.addAllowedHeader("*"); // Allows all headers
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Security filter chain configuration
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)  // Disable CSRF for stateless authentication
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))  // Apply CORS configuration
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(unauthorizedHandler)  // Handle unauthorized errors
                )
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Stateless authentication (no sessions)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/user/checkUsernameAvailability", "/api/user/checkEmailAvailability") // Public access to auth endpoints
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/polls/**", "/api/users/**") // Public access to certain GET endpoints
                        .permitAll()
                        .anyRequest().authenticated()
                );

        // Add custom JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
