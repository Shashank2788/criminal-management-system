package com.crimems.config;

import com.crimems.model.AppUser;
import com.crimems.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain chain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName(null); // load token eagerly so the JS can read the cookie

        http
            .csrf(c -> c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(csrfHandler)
                        .ignoringRequestMatchers("/login"))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/login.html", "/style.css").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")   // only admins delete
                .anyRequest().authenticated())
            .formLogin(f -> f.loginPage("/login.html").loginProcessingUrl("/login")
                             .defaultSuccessUrl("/", true).failureUrl("/login.html?error").permitAll())
            .logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/login.html"))
            .exceptionHandling(e -> e.defaultAuthenticationEntryPointFor(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), r -> r.getRequestURI().startsWith("/api/")));
        return http.build();
    }

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    UserDetailsService users(AppUserRepository repo) {
        return name -> repo.findByUsername(name)
            .map(u -> User.withUsername(u.getUsername()).password(u.getPassword()).roles(u.getRole()).build())
            .orElseThrow(() -> new UsernameNotFoundException(name));
    }

    /** Creates the two default accounts on first start. Change these passwords! */
    @Bean
    CommandLineRunner seedUsers(AppUserRepository repo, PasswordEncoder enc) {
        return args -> {
            if (repo.count() > 0) return;
            for (String[] u : new String[][]{{"admin", "admin123", "ADMIN"}, {"officer", "officer123", "OFFICER"}}) {
                AppUser a = new AppUser();
                a.setUsername(u[0]); a.setPassword(enc.encode(u[1])); a.setRole(u[2]);
                repo.save(a);
            }
        };
    }
}
