package engine.service;

import engine.model.User;
import engine.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.Set;

// log4j
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Service
public class UserService {

    Logger logger = LogManager.getLogger(UserService.class);


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    // load users from database
    public void init() {
        List<User> users = userRepository.findAll();
        printAllUsers();
    }

    @Transactional
    public void registerUser(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already taken");
        }

        User user = new User(email, passwordEncoder.encode(password), Set.of("ROLE_USER"));
        userRepository.save(user);
    }


    // debugging part
    public void printAllUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            logger.info("No users found in the system.");
        } else {
            logger.info("Users in the system:");
            for (User user : users) {
                logger.info("User ID: {}, Email: {}", user.getId(), user.getEmail());
                printRoles(user.getRoles());

            }
        }
    }

    public void printRoles(Set<String> roles) {
        logger.info("Roles assigned: {}", roles);
    }

    public void deleteAllUsers() {
        userRepository.deleteAll();
    }
}
