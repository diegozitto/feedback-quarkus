package org.feedback.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class FeedbackRequestDTO {
    private String descricao;
    private int nota;
}