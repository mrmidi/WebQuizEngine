package engine.controller;

import engine.model.*;
import engine.service.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// pagination
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@RestController
@RequestMapping("/api/quizzes")
@Validated
public class QuizController {

    private static final Logger logger = LogManager.getLogger(QuizController.class);

    private final static String SUCCESS_FEEDBACK = "Congratulations, you're right!";
    private final static String FAILURE_FEEDBACK = "Wrong answer! Please, try again.";


    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    public ResponseEntity<Question> addQuiz(@RequestBody @Valid Question question) {
        logger.info("===================================");
        logger.info("Received request to add quiz: {}", question);

        // Get the authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        logger.info("Found authenticated user: {}", userEmail);

        // Set the author field
        question.setAuthor(userEmail);

        // Save the question
        Question createdQuestion = quizService.saveQuiz(question);
        logger.info("Successfully created quiz: {}", createdQuestion);
        // return 200 OK
        return new ResponseEntity<>(createdQuestion, HttpStatus.OK);
    }

    private String getValidationErrors(ConstraintViolationException e) {
        return e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getQuizById(@PathVariable int id) {
        return quizService.getQuizById(id)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
//
//    @GetMapping
//    public List<QuestionResponse> getAllQuizzes() {
//        logger.info("Get request: all quizzes");
//        return quizService.getAllQuizzes();
//    }

    @GetMapping
    public Page<QuestionResponse> getAllQuizzes(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        logger.info("Get request: all quizzes with pagination, page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return quizService.getAllQuizzes(pageable);
    }

    @PostMapping("/{id}/solve")
    public ResponseEntity<Response> solveQuiz(@PathVariable int id, @Valid @RequestBody Answer answerRequest) {
        boolean success = quizService.solveQuiz(id, answerRequest.answer());
        return success
                ? new ResponseEntity<>(new Response(true, SUCCESS_FEEDBACK), HttpStatus.OK)
                : new ResponseEntity<>(new Response(false, FAILURE_FEEDBACK), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable int id) {
        try {
            quizService.deleteQuiz(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getStatusCode());
        }
    }

    @GetMapping("/completed")
    public ResponseEntity<Page<QuizCompletionDTO>> getCompletedQuizzes(@RequestParam(defaultValue = "0") int page) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        }

        if (userEmail == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        Pageable pageable = PageRequest.of(page, 10);
        Page<QuizCompletionDTO> completedQuizzes = quizService.getCompletedQuizzes(userEmail, pageable);
        return ResponseEntity.ok(completedQuizzes);
    }
}
