package engine.model;

import java.util.List;

public record QuestionResponse(int id, String title, String text, List<String> options) {
}
