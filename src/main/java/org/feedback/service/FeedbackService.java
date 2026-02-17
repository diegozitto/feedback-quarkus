package org.feedback.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.feedback.model.FeedbackModel;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class FeedbackService {

    // Agora permite injeção via construtor para facilitar testes
    private DynamoDbClient dynamoDb;

    @Inject
    @ConfigProperty(name = "TABLE_NAME")
    String tableName;

    // Construtor padrão usado em runtime
    public FeedbackService() {
        this.dynamoDb = DynamoDbClient.builder().build();
    }

    // Construtor para testes onde podemos injetar um mock (sem tableName)
    public FeedbackService(DynamoDbClient dynamoDb) {
        this.dynamoDb = dynamoDb;
    }

    // Construtor para testes onde podemos injetar um mock e um tableName customizado
    public FeedbackService(DynamoDbClient dynamoDb, String tableName) {
        this.dynamoDb = dynamoDb;
        this.tableName = tableName;
    }

    public void salvar(FeedbackModel feedback) {
        String id = UUID.randomUUID().toString();

        String descricaoValida = (feedback.getDescricao() == null || feedback.getDescricao().isEmpty())
                ? "Sem descrição" : feedback.getDescricao();

        Map<String, AttributeValue> item = Map.of(
                "id", AttributeValue.builder().s(id).build(),
                "descricao", AttributeValue.builder().s(descricaoValida).build(),
                "nota", AttributeValue.builder().n(String.valueOf(feedback.getNota())).build(),
                "urgencia", AttributeValue.builder().s(feedback.getUrgencia()).build(),
                "dataEnvio", AttributeValue.builder().s(feedback.getDataEnvio()).build()
        );

        dynamoDb.putItem(PutItemRequest.builder()
                .tableName(tableName != null && !tableName.isEmpty() ? tableName : "Feedbacks")
                .item(item)
                .build());
    }
}