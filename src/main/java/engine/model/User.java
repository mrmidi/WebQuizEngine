package engine.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@Entity
@Table(name = "users")
public class User {

        private static final Logger logger = LoggerFactory.getLogger(User.class);

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Email(message = "Email should be valid", regexp = ".*@.*\\..*")
        @NotBlank(message = "Email is mandatory")
        private String email;

        @Size(min = 5, message = "Password must have at least 5 characters")
        @NotBlank(message = "Password is mandatory")
        private String password;

        @ElementCollection(fetch = FetchType.EAGER)
        private Set<String> roles;

        // Default constructor
        public User() {}

        // Constructor
        public User(String email, String password, Set<String> roles) {
                logger.info("Creating user with email: {}, password: {}, roles: {}", email, password, roles);
                this.email = email;
                this.password = password;
                this.roles = roles;
        }

}