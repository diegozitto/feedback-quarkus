package org.feedback.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.List;
import java.util.Map;

@Named("gerarRelatorioHandler")
public class GerarRelatorioHandler implements RequestHandler<Map<String, Object>, String> {

    @Inject
    DynamoDbClient dynamoDb;

    @Inject
    SnsClient snsClient;

    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        ScanResponse response = dynamoDb.scan(ScanRequest.builder().tableName("Feedbacks").build());
        List<Map<String, software.amazon.awssdk.services.dynamodb.model.AttributeValue>> items = response.items();

        if (items.isEmpty()) return "Nenhum feedback encontrado para o relatório.";

        double somaNotas = 0;
        int alta = 0, media = 0, baixa = 0;

        for (var item : items) {
            somaNotas += Double.parseDouble(item.get("nota").n());
            String urgencia = item.get("urgencia").s();
            if ("ALTA".equals(urgencia)) alta++;
            else if ("MEDIA".equals(urgencia)) media++;
            else baixa++;
        }

        String relatorio = String.format(
                "--- RELATÓRIO SEMANAL DE FEEDBACKS ---\n" +
                        "Total de Avaliações: %d\n" +
                        "Média Geral: %.2f\n\n" +
                        "Distribuição por Urgência:\n" +
                        "- ALTA: %d\n- MEDIA: %d\n- BAIXA: %d",
                items.size(), (somaNotas / items.size()), alta, media, baixa
        );

        snsClient.publish(PublishRequest.builder()
                .topicArn(System.getenv("SNS_TOPIC_ARN"))
                .message(relatorio)
                .subject("Relatório Semanal de Satisfação")
                .build());

        return "Relatório gerado e enviado com sucesso!";
    }
}