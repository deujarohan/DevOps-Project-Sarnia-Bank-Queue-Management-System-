package com.queue_manage.manage_queue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public pages — anyone can access
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/display.html",
                    "/history.html",
                    "/login.html",
                    "/login"
                ).permitAll()
                // Public API endpoints — customers need these
                .requestMatchers(
                    "/api/queue/join",
                    "/api/queue/status",
                    "/api/queue/stats",
                    "/api/queue/serving",
                    "/api/queue/ticket/**",
                    "/api/analytics/**"
                ).permitAll()
                // /admin route is protected — goes through PageController
                .requestMatchers("/admin").authenticated()
                // Everything else requires login too
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/admin", true)
                .failureUrl("/login.html?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/index.html")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin123"))
            .roles("ADMIN")
            .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}