package org.feedback.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.feedback.dto.FeedbackRequestDTO;
import org.feedback.model.FeedbackModel;
import org.feedback.service.FeedbackService;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.time.LocalDateTime;

@Named("registrarFeedbackHandler")
public class RegistrarFeedbackHandler implements RequestHandler<FeedbackRequestDTO, String> {

    @Inject
    FeedbackService service;

    @Inject
    SnsClient snsClient;

    @Override
    public String handleRequest(FeedbackRequestDTO input, Context context) {

        String urgencia = (input.getNota() < 5) ? "ALTA" : "NORMAL";

        FeedbackModel model = FeedbackModel.builder()
                .descricao(input.getDescricao())
                .nota(input.getNota())
                .urgencia(urgencia)
                .dataEnvio(LocalDateTime.now().toString())
                .build();

        service.salvar(model);

        if ("ALTA".equals(urgencia)) {
            enviarAlertaCritico(input);
            return "Avaliação negativa registrada. O coordenador do curso foi notificado.";
        }

        return "Avaliação do curso registrada com sucesso!";
    }

    private void enviarAlertaCritico(FeedbackRequestDTO input) {
        String topicArn = System.getenv("SNS_TOPIC_ARN");

        String mensagem = String.format("ALERTA DE CURSO: Nota %d recebida. Comentário: %s",
                input.getNota(), input.getDescricao());

        snsClient.publish(PublishRequest.builder()
                .topicArn(topicArn)
                .message(mensagem)
                .subject("Urgência Alta: Avaliação de Curso Ruim")
                .build());
    }
}