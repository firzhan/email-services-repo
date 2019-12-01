package com.siteminder.email.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.siteminder.email.exception.EmailClientNotAvailableException;
import com.siteminder.email.model.request.EmailAddress;
import com.siteminder.email.model.request.InboundEmailMsg;
import com.siteminder.email.service.EmailServiceProviderClient;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class MailGunServiceProviderClient implements EmailServiceProviderClient {

    private final String uri = "https://api.mailgun" + ".net/v3" +
            "/sandbox5fce993a60364d1faaebcfd0842306ba.mailgun" + ".org" +
            "/messages";

    private final String username = "api";
    private final String password = "bcc48c66b5182f07baec6f6a4f0c574b" +
            "-09001d55-ff4679ab";

    @Override
    public boolean sendEmail(InboundEmailMsg inboundEmailMsg,
                             String systemSenderAddress,
                             String systemSenderName) {

        HttpPost post = new HttpPost(uri);

        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));

        // add request parameters or form parameters
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("from", systemSenderName +
                "   <" + systemSenderAddress + ">"));

        System.out.println("Sender Name:" + systemSenderName);
        System.out.println("Sender Address:" + systemSenderAddress);

        urlParameters.add(new BasicNameValuePair("subject",
                inboundEmailMsg.getSubject()));
        urlParameters.add(new BasicNameValuePair("text",
                inboundEmailMsg.getContent()));

        try {
            handleRecipients(inboundEmailMsg, urlParameters);
            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            CloseableHttpClient httpClient =
                    HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
            CloseableHttpResponse response = httpClient.execute(post);
            return response != null && (response.getStatusLine().getStatusCode() == 201
                    ||response.getStatusLine().getStatusCode() == 200) ;
        } catch (IOException e) {
            e.printStackTrace();
            throw new EmailClientNotAvailableException("Mail Gun Services Not" +
                    " Available", e.getCause());
        }
    }

    private void handleRecipients(InboundEmailMsg inboundEmailMsg,
                                  List<NameValuePair> urlParameters) throws JsonProcessingException {

        Optional<List<EmailAddress>> optionalEmailAddressList =
                Optional.ofNullable(inboundEmailMsg.getTo());
        if(optionalEmailAddressList.isPresent()){

            if(optionalEmailAddressList.get().size() > 1){

                ObjectMapper mapper = new ObjectMapper();
                ObjectNode wrapperObjectNode = mapper.createObjectNode();

                AtomicInteger count = new AtomicInteger(1);

                optionalEmailAddressList.get().forEach(emailAddress -> {
                    ObjectNode objectNode = mapper.createObjectNode();
                    objectNode.put("first", emailAddress.getName());
                    objectNode.put("id", count.getAndIncrement());
                    wrapperObjectNode.set(emailAddress.getId(), objectNode);

                });

                urlParameters.add(new BasicNameValuePair("recipient-variables",
                        mapper.writeValueAsString(wrapperObjectNode)));
            }
            optionalEmailAddressList.get().forEach(emailAddress -> { urlParameters.add(new BasicNameValuePair("to",
                        emailAddress.getId()));});
        }

        Optional.ofNullable(inboundEmailMsg.getCc()).ifPresent(emailAddressList -> {
            emailAddressList.forEach(emailAddress -> { urlParameters.add(new BasicNameValuePair("cc",
                    emailAddress.getId()));});
        });

        Optional.ofNullable(inboundEmailMsg.getBcc()).ifPresent(emailAddressList -> {
            emailAddressList.forEach(emailAddress -> { urlParameters.add(new BasicNameValuePair("bcc",
                    emailAddress.getId()));});
        });

    }
}
