package engine.model;

public record Response(boolean success, String feedback) {
    public Response {
        if (feedback == null || feedback.isEmpty()) {
            throw new IllegalArgumentException("Feedback cannot be null or empty");
        }
    }
}

