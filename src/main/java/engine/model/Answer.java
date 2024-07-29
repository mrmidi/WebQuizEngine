package engine.model;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record Answer (@NotNull List<Integer> answer) {
}
