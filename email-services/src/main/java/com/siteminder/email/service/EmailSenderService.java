package com.siteminder.email.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siteminder.email.client.MailGunServiceProviderClient;
import com.siteminder.email.client.SparkPostServiceProviderClient;
import com.siteminder.email.model.dto.EmailStore;
import com.siteminder.email.model.request.InboundEmailMsg;
import com.siteminder.email.model.state.EmailStatus;
import com.siteminder.email.repo.EmailStoreRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;

@Slf4j
@Service
public class EmailSenderService  {

    private List<EmailServiceProviderClient> emailServiceProviderClients = new LinkedList<>();

    private EmailStoreRepository emailStoreRepository;

    private CircuitBreakerRegistry circuitBreakerRegistry;

    private RetryRegistry retryRegistry;

    @Value("${email-system-sender.address}")
    private String address;

    @Value("${email-system-sender.name}")
    private String name;

    @Value("${cloud.aws.dlc.uri}")
    private String sqsDlsEndPoint;

    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;



    public EmailSenderService(CircuitBreakerRegistry circuitBreakerRegistry,
                              RetryRegistry retryRegistry,
                              EmailStoreRepository emailStoreRepository) {

        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.emailStoreRepository = emailStoreRepository;
    }

    public void addEmailClient(EmailServiceProviderClient emailServiceProviderClient) {
        emailServiceProviderClients.add(emailServiceProviderClient);
    }


    @SqsListener(value = "${cloud.aws.queue.name}", deletionPolicy =
            SqsMessageDeletionPolicy.ON_SUCCESS)
    public void consumeMessage(String id,
                               @Headers Map<String, Object> header){

        Optional<EmailStore> emailStoreOptional =
                emailStoreRepository.findById(Long.parseLong(id));

        emailStoreOptional.ifPresent(emailStore -> {

            try {
                if(!send(emailStore.getId(),
                        new ObjectMapper().readValue(emailStoreOptional.get().getContent(),
                        InboundEmailMsg.class))){
                    log.error("Email Sending " +
                            "Failed for the email : " + emailStore.toString());
                    movedToDLC(emailStore);
                }
            } catch (JsonProcessingException e) {
                log.error("Processing the request failed for the content : "
                        + emailStoreOptional.get().getContent());
            }
        });


    }

    public boolean send(long emailId, InboundEmailMsg inboundEmailMsg) {

        Optional<EmailStore> emailStoreOptional =
                emailStoreRepository.findById(emailId);

        for (EmailServiceProviderClient emailServiceProviderClient :
                emailServiceProviderClients) {
            String emailSPName =
                    emailServiceProviderClient.getClass().getSimpleName();

            log.info(String.format("EmailSenderService: trying sending email " +
                    "from %s.", emailSPName));

            CircuitBreaker circuitBreaker = circuitBreakerRegistry.
                    circuitBreaker(emailServiceProviderClient.getClass().getSimpleName());
            Retry retry = retryRegistry.retry(emailSPName);

            Supplier<Boolean> supplier =
                    CircuitBreaker.decorateSupplier(circuitBreaker,
                            () -> emailServiceProviderClient.sendEmail(inboundEmailMsg, address, name));

            supplier = Retry.decorateSupplier(retry, supplier);

            Boolean clientResponse =
                    Try.ofSupplier(supplier).recover(throwable -> false).get();
            if (clientResponse) {

                emailStoreOptional.ifPresent( emailStore -> {
                    emailStore.setEmailStatus(EmailStatus.SENT);
                    emailStoreRepository.save(emailStore);
                });
                return true;
            }
        }
/*

        //handle failed scenario
        emailStoreOptional.ifPresent(emailStore -> {
            if(emailStore.getEmailStatus() != EmailStatus.SENT)
            log.error("Email Sending " +
                "Failed for the email : " + emailStore.toString());
            movedToDLC(emailStore);
        });
*/

        return false;
    }

    private void movedToDLC(EmailStore emailStore){
        //TODO
        emailStore.setEmailStatus(EmailStatus.DLC);

        //Add the DLC part
        queueMessagingTemplate.send(sqsDlsEndPoint,
                MessageBuilder.withPayload(emailStore.getId()).build());

    }

    @PostConstruct
    private void initEmailClients() {
        emailServiceProviderClients.add(new MailGunServiceProviderClient());
        emailServiceProviderClients.add(new SparkPostServiceProviderClient());
    }
}
