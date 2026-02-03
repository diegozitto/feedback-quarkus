package org.feedback.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class FeedbackRequestDTO {
    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("nota")
    private int nota;
}