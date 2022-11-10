package io.kx.loanapp;

import io.kx.Main;
import io.kx.loanapp.api.LoanAppApi;
import io.kx.loanapp.domain.LoanAppDomainStatus;
import kalix.springsdk.testkit.KalixIntegrationTestKitSupport;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * This is a skeleton for implementing integration tests for a Kalix application built with the Spring SDK.
 *
 * This test will initiate a Kalix Proxy using testcontainers and therefore it's required to have Docker installed
 * on your machine. This test will also start your Spring Boot application.
 *
 * Since this is an integration tests, it interacts with the application using a WebClient
 * (already configured and provided automatically through injection).
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class IntegrationTest extends KalixIntegrationTestKitSupport {
  private static Logger logger = LoggerFactory.getLogger(IntegrationTest.class);
  @Autowired
  private WebClient webClient;

  private Duration timeout = Duration.of(5, ChronoUnit.SECONDS);

  @Test
  public void loanAppHappyPath() throws Exception {
    var loanAppId = UUID.randomUUID().toString();
    var submitRequest = new LoanAppApi.SubmitRequest(
            "clientId",
            5000,
            2000,
            36);
    logger.info("Sending submit...");
    ResponseEntity<LoanAppApi.EmptyResponse> emptyRes =
            webClient.post()
                    .uri("/loanapp/"+loanAppId+"/submit")
                    .bodyValue(submitRequest)
                    .retrieve()
                    .toEntity(LoanAppApi.EmptyResponse.class)
                    .block(timeout);
    assertEquals(HttpStatus.OK,emptyRes.getStatusCode());
    logger.info("Sending get...");
    LoanAppApi.GetResponse getRes =
            webClient.get()
                    .uri("/loanapp/"+loanAppId)
                    .retrieve()
                    .bodyToMono(LoanAppApi.GetResponse.class)
                    .block(timeout);

    assertEquals(LoanAppDomainStatus.STATUS_IN_REVIEW,getRes.state().status());

    logger.info("Sending approve...");
    emptyRes =
            webClient.post()
                    .uri("/loanapp/"+loanAppId+"/approve")
                    .retrieve()
                    .toEntity(LoanAppApi.EmptyResponse.class)
                    .block(timeout);

    logger.info("Sending get...");
    getRes =
            webClient.get()
                    .uri("/loanapp/"+loanAppId)
                    .retrieve()
                    .bodyToMono(LoanAppApi.GetResponse.class)
                    .block(timeout);

    assertEquals(LoanAppDomainStatus.STATUS_APPROVED,getRes.state().status());
  }
}