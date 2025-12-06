package view;

import java.util.List;

/**
 * Simple data-transfer object used by the controller to send
 * question data to the UI without exposing the model class directly.
 */
public record QuestionDTO(
        String id,
        String text,
        List<String> options,
        String levelLabel
) {}
