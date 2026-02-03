package org.feedback.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class FeedbackModel {
    private String id;
    private String descricao;
    private int nota;
    private String urgencia;
    private String dataEnvio;
}