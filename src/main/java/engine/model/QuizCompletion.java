package engine.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// lombok
import lombok.Data;


@Data
@Entity
@Table(name = "quiz_completions")
public class QuizCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Question quiz;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    // Constructors, getters, and setters
    public QuizCompletion() {
    }

    public QuizCompletion(User user, Question quiz, LocalDateTime completedAt) {
        this.user = user;
        this.quiz = quiz;
        this.completedAt = completedAt;
    }

}