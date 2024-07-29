package engine.controller;

import engine.service.QuizService;
import engine.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Admin controller.
 * This controller is created to debug the application.
 * and not part of the requirements.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private QuizService quizService;

    @DeleteMapping("/prune-users")
    public ResponseEntity<String> pruneUsers() {
        userService.deleteAllUsers();
        return ResponseEntity.ok("All users have been deleted.");
    }

    @DeleteMapping("/prune-questions")
    public ResponseEntity<String> pruneQuestions() {
        quizService.deleteAllQuizzes();
        return ResponseEntity.ok("All questions have been deleted.");
    }
}
