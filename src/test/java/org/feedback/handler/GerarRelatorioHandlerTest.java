package org.feedback.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GerarRelatorioHandlerTest {

    private GerarRelatorioHandler handler;
    private DynamoDbClient mockDynamo;
    private SnsClient mockSns;

    @BeforeEach
    public void setup() throws Exception {
        handler = new GerarRelatorioHandler();
        mockDynamo = Mockito.mock(DynamoDbClient.class);
        mockSns = Mockito.mock(SnsClient.class);

        // inject mocks into private final fields via reflection
        Field dynamoField = GerarRelatorioHandler.class.getDeclaredField("dynamoDb");
        dynamoField.setAccessible(true);
        dynamoField.set(handler, mockDynamo);

        Field snsField = GerarRelatorioHandler.class.getDeclaredField("snsClient");
        snsField.setAccessible(true);
        snsField.set(handler, mockSns);

        // inject config properties (table name and topic ARN)
        Field tableField = GerarRelatorioHandler.class.getDeclaredField("tableName");
        tableField.setAccessible(true);
        tableField.set(handler, "Feedbacks");

        Field topicField = GerarRelatorioHandler.class.getDeclaredField("snsTopicArn");
        topicField.setAccessible(true);
        topicField.set(handler, "arn:aws:sns:sa-east-1:000000000000:CriticalAlerts");
    }

    @Test
    public void testHandleRequest_noItems_returnsMessageAndNoPublish() {
        // Dynamo returns empty items
        ScanResponse emptyResp = ScanResponse.builder().items(List.of()).build();
        when(mockDynamo.scan(any(ScanRequest.class))).thenReturn(emptyResp);

        String result = handler.handleRequest(Map.of(), null);

        assertEquals("Nenhum feedback encontrado para o relatório.", result);
        verify(mockSns, never()).publish(any(PublishRequest.class));
    }

    @Test
    public void testHandleRequest_withItems_publishesReport() {
        Map<String, AttributeValue> item = Map.of(
                "id", AttributeValue.builder().s("1").build(),
                "descricao", AttributeValue.builder().s("Problema de acesso").build(),
                "nota", AttributeValue.builder().n("3").build(),
                "urgencia", AttributeValue.builder().s("ALTA").build(),
                "dataEnvio", AttributeValue.builder().s("2026-02-17T12:00:00").build()
        );

        ScanResponse resp = ScanResponse.builder().items(List.of(item)).build();
        when(mockDynamo.scan(any(ScanRequest.class))).thenReturn(resp);

        String result = handler.handleRequest(Map.of(), null);

        assertEquals("Relatório gerado e enviado com sucesso!", result);

        ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(mockSns, times(1)).publish(captor.capture());

        PublishRequest pub = captor.getValue();
        assertNotNull(pub);
        assertEquals("Relatório Semanal de Satisfação", pub.subject());
        assertEquals("arn:aws:sns:sa-east-1:000000000000:CriticalAlerts", pub.topicArn());
        assertTrue(pub.message().contains("Total de Avaliações"));
        assertTrue(pub.message().contains("ALTA"));
    }
}

