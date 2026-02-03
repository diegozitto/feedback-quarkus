package org.feedback.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.feedback.model.FeedbackModel;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class FeedbackService {

    @Inject
    DynamoDbClient dynamoDb;

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
                .tableName("Feedbacks")
                .item(item)
                .build());
    }
}