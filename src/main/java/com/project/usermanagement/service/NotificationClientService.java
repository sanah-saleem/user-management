package com.project.usermanagement.service;

import com.project.usermanagement.dto.request.EmailNotificationRequest;
import com.project.usermanagement.dto.request.NotificationOtpRequest;
import com.project.usermanagement.dto.request.NotificationOtpVerificationRequest;
import com.project.usermanagement.dto.response.NotificationOtpVerificationResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class NotificationClientService {

    private static final Logger log = LoggerFactory.getLogger(NotificationClientService.class);

    private final WebClient notificationWebClient;

    public void requestOtp(NotificationOtpRequest request) {
        try {
            notificationWebClient.post()
                    .uri("/api/otp/request")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Notification service error: " + body))
                    ).toBodilessEntity().block();
        } catch (WebClientResponseException ex) {
            log.error("Error calling notification-service /api/otp/request, status={}, body={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Error calling notification-service /api/otp/request", ex);
            throw ex;
        }
        log.info("called the notification service successfully");
    }

    public NotificationOtpVerificationResponse verifyOtp(NotificationOtpVerificationRequest request) {
        try {
            return notificationWebClient.post()
                    .uri("/api/otp/verify")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Notification service error: " + body))
                    ).bodyToMono(NotificationOtpVerificationResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("Error calling notification-service /api/otp/verify, status={}, body={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Error calling notification-service /api/otp/verify", ex);
            throw ex;
        }
    }

    public void sendEmailNotification(EmailNotificationRequest request) {
        try {
            notificationWebClient.post()
                    .uri("/api/notifications/email")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Notification service error: " + body))
                    ).toBodilessEntity()
                    .block();
            log.info("Queued email notification for {}", request.to());
        } catch (WebClientResponseException ex) {
            // we dont want to stop the main operation like user registration. so just log and continue
            log.error("Error calling notification-service /api/notifications/email, status={}, body={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            log.error("Error calling notification-service /api/notifications/email", ex);
        }
    }

}
