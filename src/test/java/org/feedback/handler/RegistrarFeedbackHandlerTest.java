package org.feedback.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.feedback.service.FeedbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RegistrarFeedbackHandlerTest {

    private RegistrarFeedbackHandler handler;
    private FeedbackService mockService;
    private SnsClient mockSns;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() throws Exception {
        handler = new RegistrarFeedbackHandler();
        mockService = Mockito.mock(FeedbackService.class);
        mockSns = Mockito.mock(SnsClient.class);

        // inject service
        Field serviceField = RegistrarFeedbackHandler.class.getDeclaredField("service");
        serviceField.setAccessible(true);
        serviceField.set(handler, mockService);

        // inject sns client
        Field snsField = RegistrarFeedbackHandler.class.getDeclaredField("snsClient");
        snsField.setAccessible(true);
        snsField.set(handler, mockSns);

        // inject objectMapper
        Field mapperField = RegistrarFeedbackHandler.class.getDeclaredField("objectMapper");
        mapperField.setAccessible(true);
        mapperField.set(handler, objectMapper);

        // inject sns topic arn
        Field topicField = RegistrarFeedbackHandler.class.getDeclaredField("snsTopicArn");
        topicField.setAccessible(true);
        topicField.set(handler, "arn:aws:sns:sa-east-1:000000000000:CriticalAlerts");

        // stub publish to return a response
        when(mockSns.publish(any(PublishRequest.class))).thenReturn(PublishResponse.builder().messageId("msg-1").build());
    }

    @Test
    public void testHandleRequest_highUrgency_publishesSns_and_returns200() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("descricao", "Problema grave", "nota", 3));
        APIGatewayProxyRequestEvent req = new APIGatewayProxyRequestEvent();
        req.setBody(body);

        APIGatewayProxyResponseEvent resp = handler.handleRequest(req, null);

        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode());
        assertTrue(resp.getBody().contains("coordenador"));

        // Verify service.salvar called
        verify(mockService, times(1)).salvar(any());

        // Verify sns publish called with correct topic
        ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(mockSns, times(1)).publish(captor.capture());

        PublishRequest pub = captor.getValue();
        assertEquals("arn:aws:sns:sa-east-1:000000000000:CriticalAlerts", pub.topicArn());
        assertTrue(pub.message().contains("Problema grave"));
    }

    @Test
    public void testHandleRequest_lowUrgency_noSnsPublish_and_returns200() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("descricao", "Tudo ok", "nota", 9));
        APIGatewayProxyRequestEvent req = new APIGatewayProxyRequestEvent();
        req.setBody(body);

        APIGatewayProxyResponseEvent resp = handler.handleRequest(req, null);

        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode());
        assertTrue(resp.getBody().contains("Avaliação registrada"));

        verify(mockService, times(1)).salvar(any());
        verify(mockSns, never()).publish(any(PublishRequest.class));
    }
}

