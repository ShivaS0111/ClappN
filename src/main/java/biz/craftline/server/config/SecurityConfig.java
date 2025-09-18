package biz.craftline.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableAspectJAutoProxy
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 👈 add this
                .csrf(AbstractHttpConfigurer::disable)
                //.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        //.requestMatchers(getPublicEndPoints()).permitAll()
                        .requestMatchers("/**").permitAll()
                        /*
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/users/list").hasAuthority("ADMIN")
                        .requestMatchers("/roles/list").hasAuthority("SUPER_ADMIN")
                        .requestMatchers("/business-types/**").hasAuthority("SUPER_ADMIN")
                        .requestMatchers("/service/**").hasAuthority("SUPER_ADMIN")
                        .requestMatchers("/business/**").hasAuthority("SUPER_ADMIN")
                        .requestMatchers("/stores/**").hasAuthority("SUPER_ADMIN")
                        */

                        .anyRequest()
                        .authenticated()).csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(
                                (request, response, authException) -> response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized access")));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:5173")); // 👈 allow frontend
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private String[] getPublicEndPoints() {
        return new String[]{
                "/public/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/api/swagger-ui.html",
                "/api/v3/api-docs",
                "/api/v3/api-docs/**",
                "/webjars/**"
        };
    }
}
