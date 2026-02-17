package org.feedback.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import jakarta.inject.Named;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Map;

@Named("gerarRelatorioHandler")
public class GerarRelatorioHandler implements RequestHandler<Map<String, Object>, String> {

    private final DynamoDbClient dynamoDb = DynamoDbClient.builder().build();
    private final SnsClient snsClient = SnsClient.builder().build();

    @ConfigProperty(name = "TABLE_NAME", defaultValue = "Feedbacks")
    String tableName;

    @ConfigProperty(name = "SNS_TOPIC_ARN", defaultValue = "")
    String snsTopicArn;

    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        ScanResponse response = dynamoDb.scan(ScanRequest.builder().tableName(tableName != null && !tableName.isEmpty() ? tableName : "Feedbacks").build());
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

        String template = """
                --- RELATÓRIO SEMANAL DE FEEDBACKS ---
                Total de Avaliações: %d
                Média Geral: %.2f

                Distribuição por Urgência:
                - ALTA: %d
                - MEDIA: %d
                - BAIXA: %d
                """;

        String relatorio = String.format(template, items.size(), (somaNotas / items.size()), alta, media, baixa);

        String topic = snsTopicArn != null && !snsTopicArn.isEmpty() ? snsTopicArn : System.getenv("SNS_TOPIC_ARN");

        snsClient.publish(PublishRequest.builder()
                .topicArn(topic)
                .message(relatorio)
                .subject("Relatório Semanal de Satisfação")
                .build());

        return "Relatório gerado e enviado com sucesso!";
    }
}