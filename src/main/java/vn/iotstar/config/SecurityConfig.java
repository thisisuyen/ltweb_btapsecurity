package vn.iotstar.config;

import vn.iotstar.security.CustomAuthSuccessHandler;
import vn.iotstar.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder);
        return p;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authProvider,
            CustomAuthSuccessHandler successHandler
    ) throws Exception {

        http.authenticationProvider(authProvider);

        
        http.csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests(auth -> auth
               
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/uploads/**").permitAll()

               
                .requestMatchers("/auth/**").permitAll() 
                .requestMatchers("/admin/**").hasRole("ADMIN")

               
                .requestMatchers("/products/**").authenticated()

             
                .anyRequest().authenticated()
        );

        http.formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .usernameParameter("username") 
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureUrl("/auth/login?error=true")
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
        );

        http.exceptionHandling(e -> e.accessDeniedPage("/403"));

        return http.build();
    }
}