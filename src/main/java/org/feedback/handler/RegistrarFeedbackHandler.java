package org.feedback.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.feedback.dto.FeedbackRequestDTO;
import org.feedback.model.FeedbackModel;
import org.feedback.service.FeedbackService;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.time.LocalDateTime;
import java.util.Map;

@Named("registrarFeedbackHandler")
public class RegistrarFeedbackHandler implements RequestHandler<FeedbackRequestDTO, APIGatewayProxyResponseEvent> {

    @Inject
    FeedbackService service;

    @Inject
    SnsClient snsClient;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(FeedbackRequestDTO input, Context context) {
        try {

            String urgencia;
            if (input.getNota() <= 5) {
                urgencia = "ALTA";
            } else if (input.getNota() >= 6 && input.getNota() <= 7) {
                urgencia = "MEDIA";
            } else {
                urgencia = "BAIXA";
            }

            FeedbackModel model = FeedbackModel.builder()
                    .descricao(input.getDescricao())
                    .nota(input.getNota())
                    .urgencia(urgencia)
                    .dataEnvio(LocalDateTime.now().toString())
                    .build();

            service.salvar(model);

            String mensagemRetorno;

            if ("ALTA".equals(urgencia)) {
                enviarAlertaCritico(input);
                mensagemRetorno = "Avaliação negativa registrada. O coordenador foi notificado.";
            } else {
                mensagemRetorno = "Avaliação registrada com sucesso! Prioridade: " + urgencia;
            }

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(Map.of("Content-Type", "application/json"))
                    .withBody(String.format("{\"message\": \"%s\"}", mensagemRetorno));

        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody(String.format("{\"error\": \"%s\"}", e.getMessage()));
        }
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