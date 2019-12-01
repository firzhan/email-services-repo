package com.siteminder.email.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.siteminder.email.EmailServiceApplication;
import com.siteminder.email.model.request.EmailAddress;
import com.siteminder.email.model.request.InboundEmailMsg;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
@SpringBootTest(classes = EmailServiceApplication.class, webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(locations = "/application-integration.properties")
@ActiveProfiles("integration")
class EmailServiceApplicationIT {

    @LocalServerPort
    private int port;



    @Test
    void Round_Trip_And_Check_Return_Statuses() {

        InboundEmailMsg inboundEmailMsg = new InboundEmailMsg();

        EmailAddress emailAddress = new EmailAddress();
        emailAddress.setId("firzhan007@gmail.com");
        emailAddress.setName("firzhan");

        List<EmailAddress> emailAddresses = new ArrayList<>();
        emailAddresses.add(emailAddress);
        inboundEmailMsg.setTo(emailAddresses);

        inboundEmailMsg.setContent("Hello");
        inboundEmailMsg.setSubject("Hello");

        Response response =
                given().contentType(ContentType.JSON).body(inboundEmailMsg).
                post("http://localhost:" + port + "/email/submit");

        Assert.assertEquals("ENQUEUED",
                response.then().statusCode(HttpStatus.OK.value()).
                extract().jsonPath().getString("email-status"));

        long emailRefId = response.then().extract().jsonPath().getLong("email"
                + "-store-id");

        try {
            TimeUnit.SECONDS.sleep(15);
        } catch (InterruptedException e) {
            log.error("Integration Test Thread sleep got interrupted", e);
        }

        given().accept(ContentType.JSON).
                when().
                get("http://localhost:" + port + "/email/status/" + emailRefId).
                then().
                statusCode(200).
                assertThat().
                body("email-status", equalTo("SENT")).
                body("email-store-id",
                        equalTo(new Long(emailRefId).intValue())).
                body("http-status-code", equalTo(200));
        //https://github.com/rest-assured/rest-assured/issues/741

    }

}
