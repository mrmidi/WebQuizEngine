package engine.config;

import engine.model.User;
import engine.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.logging.Logger;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class WebSecurityConfig {

    private static final Logger logger = Logger.getLogger(WebSecurityConfig.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/api/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/register").permitAll()
                        .requestMatchers("/api/admin/**").permitAll() // This is for debugging purposes only
                        .requestMatchers("/admin.html").permitAll() // This is for debugging purposes only
                        .requestMatchers("/api/quizzes/**").authenticated()
                        .requestMatchers("/actuator/shutdown").permitAll() // for hyperskill tests
                        .requestMatchers("/h2-console/**").permitAll() // debug
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new UsernameNotFoundException("User not found: " + email);
            }
            logger.info("User found: " + user);
            return org.springframework.security.core.userdetails.User.withUsername(email)
                    .password(user.getPassword())
                    .roles(user.getRoles().stream().map(role -> role.substring(5)).toArray(String[]::new)) // Remove "ROLE_" prefix
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
