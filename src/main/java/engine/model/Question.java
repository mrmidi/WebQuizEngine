package engine.model;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@Entity
@Table(name = "questions")
public class Question {

    private static final Logger logger = LoggerFactory.getLogger(Question.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @NotEmpty
    private String title;

    @NotNull
    @NotEmpty
    private String text;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @Size(min = 2, message = "The number of options must be at least 2")
    private List<String> options = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Integer> answer = new ArrayList<>();

    private String author; // the checks are done in the controller
                            // since it's not user-provided data

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizCompletion> completions = new ArrayList<>();

    public Question() {}

    public Question(int id, String title, String text, List<String> options, List<Integer> answer, String author) {
        logger.warn("=========================================");
        logger.warn("DTOQuestion constructor");
        logger.info("Arguments: id={}, title={}, text={}, options={}, answer={}, author={}", id, title, text, options, answer, author);
        logger.info("Options: {}", options);
        logger.warn("=========================================");
        this.id = id;
        this.title = title;
        this.text = text;
        try {
            this.options = options;
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage());
        }
        this.answer = answer != null ? answer : new ArrayList<>();
        this.author = author;
    }

    public boolean isCorrect(List<Integer> answer) {
        List<Integer> sortedThisAnswer = new ArrayList<>(this.answer);
        List<Integer> sortedAnswer = new ArrayList<>(answer);
        sortedThisAnswer.sort(Integer::compareTo);
        sortedAnswer.sort(Integer::compareTo);
        return sortedThisAnswer.equals(sortedAnswer);
    }
}
