package biz.craftline.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableAspectJAutoProxy
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(getPublicEndPoints()).permitAll()
                        /*.requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/users/list").hasAuthority("ADMIN")
                        .requestMatchers("/roles/list").hasAuthority("SUPER_ADMIN")
                        //.requestMatchers("/business-types/**").hasAuthority("SUPER_ADMIN")
                        .requestMatchers("/service/**").hasAuthority("SUPER_ADMIN")
                        .requestMatchers("/business/**").hasAuthority("SUPER_ADMIN")
                        .requestMatchers("/stores/**").hasAuthority("SUPER_ADMIN")*/
                        .anyRequest()
                        .authenticated()).csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(
                                (request, response, authException) -> response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized access")));
        return http.build();
    }

    private String[] getPublicEndPoints() {
        return new String[]{"/public/**", "/swagger-ui/**", "/**"};
    }
}

