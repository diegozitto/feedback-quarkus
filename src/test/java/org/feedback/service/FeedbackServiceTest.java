package org.feedback.service;

import org.feedback.model.FeedbackModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FeedbackServiceTest {

    private DynamoDbClient dynamoDbClient;
    private FeedbackService feedbackService;

    @BeforeEach
    public void setup() {
        dynamoDbClient = Mockito.mock(DynamoDbClient.class);
        feedbackService = new FeedbackService(dynamoDbClient);
    }

    @Test
    public void testSalvar_callsPutItemWithExpectedValues() {
        FeedbackModel model = FeedbackModel.builder()
                .descricao("Teste unitario")
                .nota(4)
                .urgencia("ALTA")
                .dataEnvio("2026-02-17T12:00:00")
                .build();

        // executar
        feedbackService.salvar(model);

        // capturar o argumento
        ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDbClient, times(1)).putItem(captor.capture());

        PutItemRequest request = captor.getValue();
        assertNotNull(request);
        assertEquals("Feedbacks", request.tableName());

        Map<String, AttributeValue> item = request.item();
        assertEquals("Teste unitario", item.get("descricao").s());
        assertEquals("4", item.get("nota").n());
        assertEquals("ALTA", item.get("urgencia").s());
        assertEquals("2026-02-17T12:00:00", item.get("dataEnvio").s());
        assertTrue(item.containsKey("id"));
        assertFalse(item.get("id").s().isEmpty());
    }

    @Test
    public void testSalvar_usesCustomTableNameWhenProvided() throws Exception {
        FeedbackModel model = FeedbackModel.builder()
                .descricao("Teste custom table")
                .nota(7)
                .urgencia("MEDIA")
                .dataEnvio("2026-02-17T13:00:00")
                .build();

        // usar construtor que aceita tableName customizado
        feedbackService = new FeedbackService(dynamoDbClient, "CustomFeedbacks");

        feedbackService.salvar(model);

        ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDbClient, times(1)).putItem(captor.capture());

        PutItemRequest request = captor.getValue();
        assertEquals("CustomFeedbacks", request.tableName());

        Map<String, AttributeValue> item = request.item();
        assertEquals("Teste custom table", item.get("descricao").s());
        assertEquals("7", item.get("nota").n());
        assertEquals("MEDIA", item.get("urgencia").s());
    }
}
