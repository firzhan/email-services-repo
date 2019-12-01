package com.siteminder.email.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.siteminder.email.exception.EmailClientNotAvailableException;
import com.siteminder.email.model.request.EmailAddress;
import com.siteminder.email.model.request.InboundEmailMsg;
import com.siteminder.email.service.EmailServiceProviderClient;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class SparkPostServiceProviderClient implements EmailServiceProviderClient {

    private final static String uri = "https://api.sparkpost" + ".com/api/v1" +
            "/transmissions";
    private final static String authorizationCode =
            "a0cea0220ee6ba0929532dcc3740c85e52cc8699";

    @Override
    public boolean sendEmail(InboundEmailMsg inboundEmailMsg,
                             String systemSenderAddress,
                             String systemSenderName) {

        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader("Authorization", authorizationCode);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        StringEntity stringEntity = null;
        try {
            stringEntity =
                    new StringEntity(generateEntityPayload(inboundEmailMsg,
                            systemSenderAddress));
            httpPost.setEntity(stringEntity);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(httpPost);
            return response != null && response.getStatusLine().getStatusCode() == 200;
        } catch (IOException e) {
            throw new EmailClientNotAvailableException("Spark Post Mail " +
                    "Services Not Available", e.getCause());
        }

    }

    private String generateEntityPayload(InboundEmailMsg inboundEmailMsg,
                                         String systemSenderAddress) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode wrapperObjectNode = mapper.createObjectNode();

        ObjectNode sandboxNode = mapper.createObjectNode();
        sandboxNode.put("sandbox", true);

        wrapperObjectNode.set("options", sandboxNode);

        ObjectNode contentsNode = mapper.createObjectNode();
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

                ObjectNode ccNode = mapper.createObjectNode();
                ccNode.put("CC", ccBuilder.toString());

                contentsNode.set("headers", ccNode);

            }
        });

        contentsNode.put("from", systemSenderAddress);
        contentsNode.put("subject", inboundEmailMsg.getSubject());
        contentsNode.put("text", inboundEmailMsg.getContent());

        ArrayNode recipientArrayNode = mapper.createArrayNode();
        wrapperObjectNode.set("recipients", recipientArrayNode);

        populateRecipients(Optional.ofNullable(inboundEmailMsg.getTo()),
                mapper, systemSenderAddress, recipientArrayNode);

        populateRecipients(Optional.ofNullable(inboundEmailMsg.getCc()),
                mapper, systemSenderAddress, recipientArrayNode);

        populateRecipients(Optional.ofNullable(inboundEmailMsg.getBcc()),
                mapper, systemSenderAddress, recipientArrayNode);

        return mapper.writeValueAsString(wrapperObjectNode);
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

