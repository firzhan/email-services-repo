package com.siteminder.email.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.siteminder.email.client.config.MailClientConfig;
import com.siteminder.email.exception.EmailClientNotAvailableException;
import com.siteminder.email.model.request.EmailAddress;
import com.siteminder.email.model.request.InboundEmailMsg;
import com.siteminder.email.service.EmailServiceProviderClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class SparkPostServiceProviderClient implements EmailServiceProviderClient {

    private ObjectMapper objectMapper;

    private RestTemplate restTemplate;

    private MailClientConfig mailClientConfig;

    public SparkPostServiceProviderClient(ObjectMapper objectMapper,
                                          RestTemplate restTemplate,
                                          MailClientConfig mailClientConfig) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.mailClientConfig = mailClientConfig;
    }

    public boolean sendEmail(InboundEmailMsg inboundEmailMsg,
                             String systemSenderAddress,
                             String systemSenderName) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", this.mailClientConfig.getSparkAuthCode());

        try {
            HttpEntity<String> request =
                    new HttpEntity<>(generateEntityPayload(inboundEmailMsg,
                            systemSenderAddress), headers);
            ResponseEntity<String> response =
                    restTemplate.postForEntity(this.mailClientConfig.getSparkPostURI(),
                    request, String.class);
            return response.getStatusCode().value() == 201 || response.getStatusCode().value() == 200;
        } catch (IOException e) {
            throw new EmailClientNotAvailableException("Spark Post Mail " +
                    "Services Not Available", e.getCause());
        }
    }

    private String generateEntityPayload(InboundEmailMsg inboundEmailMsg,
                                         String systemSenderAddress) throws JsonProcessingException {

        ObjectNode wrapperObjectNode = objectMapper.createObjectNode();

        ObjectNode sandboxNode = objectMapper.createObjectNode();
        sandboxNode.put("sandbox", true);

        wrapperObjectNode.set("options", sandboxNode);

        ObjectNode contentsNode = objectMapper.createObjectNode();
        wrapperObjectNode.set("content", contentsNode);
        Optional<List<EmailAddress>> optionalEmailAddressList =
                Optional.ofNullable(inboundEmailMsg.getCc());

        optionalEmailAddressList.ifPresent(emailAddresses -> {
            if (!emailAddresses.isEmpty()) {
                StringBuilder ccBuilder = new StringBuilder();
                AtomicInteger count = new AtomicInteger(1);
                emailAddresses.forEach((emailAddress) -> {
                    if (count.getAndIncrement() == 1) {
                        ccBuilder.append(emailAddress);
                    } else {
                        ccBuilder.append(",").append(emailAddress);
                    }
                });

                ObjectNode ccNode = objectMapper.createObjectNode();
                ccNode.put("CC", ccBuilder.toString());

                contentsNode.set("headers", ccNode);

            }
        });

        contentsNode.put("from", systemSenderAddress);
        contentsNode.put("subject", inboundEmailMsg.getSubject());
        contentsNode.put("text", inboundEmailMsg.getContent());

        ArrayNode recipientArrayNode = objectMapper.createArrayNode();
        wrapperObjectNode.set("recipients", recipientArrayNode);

        populateRecipients(Optional.ofNullable(inboundEmailMsg.getTo()),
                objectMapper, systemSenderAddress, recipientArrayNode);

        populateRecipients(Optional.ofNullable(inboundEmailMsg.getCc()),
                objectMapper, systemSenderAddress, recipientArrayNode);

        populateRecipients(Optional.ofNullable(inboundEmailMsg.getBcc()),
                objectMapper, systemSenderAddress, recipientArrayNode);

        return objectMapper.writeValueAsString(wrapperObjectNode);
    }

    private void populateRecipients(Optional<List<EmailAddress>> optionalEmailAddressList, ObjectMapper mapper, String systemSenderAddress, ArrayNode recipientArrayNode) {

        optionalEmailAddressList.ifPresent(emailAddressList -> {

            emailAddressList.forEach(emailAddress -> {
                ObjectNode recipientNode = mapper.createObjectNode();
                recipientNode.put("address", emailAddress.getId());
                recipientNode.put("header_to", systemSenderAddress);

                recipientArrayNode.add(recipientNode);
            });
        });

    }
}

