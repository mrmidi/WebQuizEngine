package engine.model;

import java.time.LocalDateTime;

// lombok
import lombok.Data;

@Data
public class QuizCompletionDTO {

    private int id;
    private LocalDateTime completedAt;

    public QuizCompletionDTO(int quizId, LocalDateTime completedAt) {
        this.id = quizId;
        this.completedAt = completedAt;
    }
}
