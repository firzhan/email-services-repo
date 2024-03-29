package com.siteminder.email.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siteminder.email.client.MailGunServiceProviderClient;
import com.siteminder.email.client.SparkPostServiceProviderClient;
import com.siteminder.email.client.config.MailClientConfig;
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
import org.springframework.web.client.RestTemplate;

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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    MailClientConfig mailClientConfig;

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

    /**
     * Continuously polls the queue to fetch any pending requests.
     * @param id
     * @param header
     */
    @SqsListener(value = "${cloud.aws.queue.name}", deletionPolicy =
            SqsMessageDeletionPolicy.ON_SUCCESS)
    public void consumeMessage(String id,
                               @Headers Map<String, Object> header){

        Optional<EmailStore> emailStoreOptional =
                emailStoreRepository.findById(Long.parseLong(id));

        emailStoreOptional.ifPresent(emailStore -> {

            try {
                if(!send(emailStore.getId(),
                        objectMapper.readValue(emailStoreOptional.get().getContent(),
                        InboundEmailMsg.class))){
                    log.error("Email Sending " +
                            "Failed for the email : " + emailStore.toString());
                    movedToDLC(emailStore);
                }
            } catch (JsonProcessingException e) {
                log.error("Processing the request failed for the content : "
                        + emailStoreOptional.get().getContent(), e);
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

            log.debug(String.format("EmailSenderService: Attempt to send " +
                    "mail from %s.", emailSPName));

            CircuitBreaker circuitBreaker = circuitBreakerRegistry.
                    circuitBreaker(emailServiceProviderClient.getClass().getSimpleName());
            Retry retry = retryRegistry.retry(emailSPName);

            Supplier<Boolean> supplier =
                    CircuitBreaker.decorateSupplier(circuitBreaker,
                            () -> emailServiceProviderClient.sendEmail(inboundEmailMsg, address, name));

            supplier = Retry.decorateSupplier(retry, supplier);

            if (Try.ofSupplier(supplier).recover(throwable -> false).get()) {

                emailStoreOptional.ifPresent( emailStore -> {
                    emailStore.setEmailStatus(EmailStatus.SENT);
                    emailStoreRepository.save(emailStore);
                });
                log.debug(String.format("EmailSenderService: Mail is posted " +
                        "successfully to the Mail Service Provider %s.",
                        emailSPName));
                return true;
            }
        }
        return false;
    }

    /**
     * This method basically stores the failed messages into a DLC queue.
     * This method could be improved with retry mechanism to send the message
     * to the service providers.
     * @param emailStore Object to be moved to the DLC with the new email status
     */

    private void movedToDLC(EmailStore emailStore){
        emailStore.setEmailStatus(EmailStatus.DLC);
        queueMessagingTemplate.send(sqsDlsEndPoint,
                MessageBuilder.withPayload(emailStore.getId()).build());
        emailStoreRepository.save(emailStore);


    }

    /**
     * A new client has to be added here
     */
    @PostConstruct
    private void initEmailClients() {
        addEmailClient(new MailGunServiceProviderClient(objectMapper, restTemplate, mailClientConfig));
        addEmailClient(new SparkPostServiceProviderClient(objectMapper, restTemplate, mailClientConfig));
    }
}
