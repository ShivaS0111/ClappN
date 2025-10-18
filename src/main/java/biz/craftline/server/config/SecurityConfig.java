package biz.craftline.server.config;

import biz.craftline.server.config.security.JwtAuthenticationFilter;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserService userService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()

                // Permission-based authorization using hierarchical authorities
                .requestMatchers(POST, "/api/users/permissions/**")
                    .hasAuthority("MANAGE_USER_PERMISSIONS")
                .requestMatchers(DELETE, "/api/users/permissions/**")
                    .hasAuthority("MANAGE_USER_PERMISSIONS")
                .requestMatchers(GET, "/api/users/permissions/details/**")
                    .hasAuthority("VIEW_USER_PERMISSIONS")

                .requestMatchers(POST, "/api/users")
                    .hasAuthority("CREATE_USER")
                .requestMatchers(PUT, "/api/users/**")
                    .hasAnyAuthority("UPDATE_USER", "USER_MANAGEMENT")
                .requestMatchers(DELETE, "/api/users/**")
                    .hasAuthority("DELETE_USER")
                .requestMatchers(GET, "/api/users/**")
                    .hasAnyAuthority("VIEW_USERS", "USER_MANAGEMENT")

                .requestMatchers("/api/admin/**")
                    .hasAuthority("ADMIN_ACCESS")
                .requestMatchers("/api/reports/**")
                    .hasAuthority("VIEW_REPORTS")

                // Protected endpoints - require authentication
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            try {
                return userService.loadUserByUsername(username);
            } catch (Exception e) {
                throw new UsernameNotFoundException("User not found: " + username);
            }
        };
    }
}
