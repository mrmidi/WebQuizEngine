package engine.service;

import engine.model.*;
import engine.repository.QuestionRepository;
import engine.repository.QuizCompletionRepository;
import engine.repository.UserRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

// pagination
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;


@Service
public class QuizService {
    private static final Logger logger = getLogger(QuizService.class);

    private final QuestionRepository quizRepository;
    private List<Question> quizzes;
    private final AtomicInteger counter = new AtomicInteger();

    // user repository
    private UserRepository userRepository;
    private QuizCompletionRepository quizCompletionRepository;

    @Autowired
    public QuizService(QuestionRepository quizRepository, UserRepository userRepository, QuizCompletionRepository quizCompletionRepository) {
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.quizCompletionRepository = quizCompletionRepository;
    }

    @PostConstruct
    private void init() {
        quizzes = quizRepository.findAll();
        if (!quizzes.isEmpty()) {
            counter.set(quizzes.stream().mapToInt(Question::getId).max().orElse(0));
        }
    }

    public QuestionResponse addQuiz(Question quiz) {
        logger.info("Adding quiz: {}", quiz);
        Question newQuiz = new Question(quiz.getId(), quiz.getTitle(), quiz.getText(), quiz.getOptions(), quiz.getAnswer(), getCurrentUsername());
        quizRepository.save(newQuiz);
        return new QuestionResponse(newQuiz.getId(), newQuiz.getTitle(), newQuiz.getText(), newQuiz.getOptions());
    }

    public Optional<QuestionResponse> getQuizById(int id) {
        return quizRepository.findById(id)
                .map(quiz -> new QuestionResponse(quiz.getId(), quiz.getTitle(), quiz.getText(), List.of(quiz.getOptions().toArray(new String[0]))));
    }

    public Page<QuestionResponse> getAllQuizzes(Pageable pageable) {
        return quizRepository.findAll(pageable)
                .map(quiz -> new QuestionResponse(quiz.getId(), quiz.getTitle(), quiz.getText(), List.of(quiz.getOptions().toArray(new String[0]))));
    }

//    public boolean solveQuiz(int id, @NotNull List<Integer> answers) {
//        return quizRepository.findById(id)
//                .map(quiz -> quiz.isCorrect(answers))
//                .orElse(false);
//    }

    public boolean solveQuiz(int id, @NotNull List<Integer> answers) {
        Optional<Question> quizOptional = quizRepository.findById(id);
        if (quizOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found");
        }
        Question quiz = quizOptional.get();
        boolean success = quiz.isCorrect(answers);

        // Store the quiz completion
        String currentUser = getCurrentUsername();
        User user = userRepository.findByEmail(currentUser);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        if (success) {
            logger.info("Storing quiz completion for user: {} as it was solved correctly", user.getEmail());
            QuizCompletion completion = new QuizCompletion();
            completion.setQuiz(quiz);
            completion.setUser(user);
            completion.setCompletedAt(LocalDateTime.now());
            quizCompletionRepository.save(completion);
        }


        return success;
    }

    public void deleteQuiz(int id) {
        Optional<Question> optionalQuiz = quizRepository.findById(id);
        if (optionalQuiz.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found");
        }

        Question quiz = optionalQuiz.get();
        String currentUser = getCurrentUsername();

        if (!quiz.getAuthor().equals(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the author of this quiz");
        }

        quizRepository.delete(quiz);
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }


    // delete all quizzes
    public void deleteAllQuizzes() {
        quizRepository.deleteAll();
    }

    public Question saveQuiz(Question question) {
        return quizRepository.save(question);
    }

    public Page<QuizCompletionDTO> getCompletedQuizzes(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        logger.info("Fetching completed quizzes for user: {}", user.getEmail());

        Page<QuizCompletion> completionsPage = quizCompletionRepository.findByUserOrderByCompletedAtDesc(user, pageable);
        List<QuizCompletionDTO> completionsDTO = completionsPage.getContent().stream()
                .map(completion -> new QuizCompletionDTO(completion.getQuiz().getId(), completion.getCompletedAt()))
                .collect(Collectors.toList());

        return new PageImpl<>(completionsDTO, pageable, completionsPage.getTotalElements());
    }
}
