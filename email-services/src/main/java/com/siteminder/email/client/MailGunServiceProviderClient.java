package com.siteminder.email.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.siteminder.email.client.config.MailClientConfig;
import com.siteminder.email.exception.EmailClientNotAvailableException;
import com.siteminder.email.model.request.EmailAddress;
import com.siteminder.email.model.request.InboundEmailMsg;
import com.siteminder.email.service.EmailServiceProviderClient;
import com.siteminder.email.util.EmailServiceUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class MailGunServiceProviderClient implements EmailServiceProviderClient {

    private ObjectMapper objectMapper;

    private RestTemplate restTemplate;

    private MailClientConfig mailClientConfig;

    public MailGunServiceProviderClient(ObjectMapper objectMapper,
                                        RestTemplate restTemplate,
                                        MailClientConfig mailClientConfig) {

        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.mailClientConfig = mailClientConfig;
    }

    @Override
    public boolean sendEmail(InboundEmailMsg inboundEmailMsg,
                             String systemSenderAddress,
                             String systemSenderName) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization",
                EmailServiceUtils.generateAuthHeaderValue(this.mailClientConfig.getMailGunUsername(),
                        this.mailClientConfig.getMailGunPassword()));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("from", systemSenderName + "   <" + systemSenderAddress + ">");
        map.add("subject", inboundEmailMsg.getSubject());
        map.add("text", inboundEmailMsg.getContent());

        try {
            handleRecipients(inboundEmailMsg, map);
            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(map, headers);
            ResponseEntity<String> response =
                    restTemplate.postForEntity(this.mailClientConfig.getMailGunURI(),
                    request, String.class);
            return response.getStatusCode().value() == 201 || response.getStatusCode().value() == 200;
        } catch (IOException e) {
            throw new EmailClientNotAvailableException("Mail Gun Services " + "Not" + " Available", e.getCause());
        }
    }

    private void handleRecipients(InboundEmailMsg inboundEmailMsg,
                                  MultiValueMap<String, String> map) throws JsonProcessingException {

        Optional<List<EmailAddress>> optionalEmailAddressList =
                Optional.ofNullable(inboundEmailMsg.getTo());
        if (optionalEmailAddressList.isPresent()) {

            if (optionalEmailAddressList.get().size() > 1) {

                ObjectNode wrapperObjectNode = objectMapper.createObjectNode();

                AtomicInteger count = new AtomicInteger(1);

                optionalEmailAddressList.get().forEach(emailAddress -> {
                    ObjectNode objectNode = objectMapper.createObjectNode();
                    objectNode.put("first", emailAddress.getName());
                    objectNode.put("id", count.getAndIncrement());
                    wrapperObjectNode.set(emailAddress.getId(), objectNode);

                });

                map.add("recipient-variables",
                        objectMapper.writeValueAsString(wrapperObjectNode));
            }

            optionalEmailAddressList.get().forEach(emailAddress -> {
                map.add("to", emailAddress.getId());
            });
        }

        Optional.ofNullable(inboundEmailMsg.getCc()).ifPresent(emailAddressList -> {
            emailAddressList.forEach(emailAddress -> {
                map.add("cc", emailAddress.getId());
            });
        });

        Optional.ofNullable(inboundEmailMsg.getBcc()).ifPresent(emailAddressList -> {
            emailAddressList.forEach(emailAddress -> {
                map.add("bcc", emailAddress.getId());
            });
        });

    }
}
