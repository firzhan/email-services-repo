package com.siteminder.email.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siteminder.email.exception.EmailPayloadProcessingException;
import com.siteminder.email.exception.EmailRecordNotFoundException;
import com.siteminder.email.model.dto.EmailStore;
import com.siteminder.email.model.request.InboundEmailMsg;
import com.siteminder.email.model.state.EmailStatus;
import com.siteminder.email.repo.EmailStoreRepository;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Slf4j
@Service
@NoArgsConstructor
public class EmailPersistingService {

    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;

    @Autowired
    private EmailStoreRepository emailStoreRepository;

    @Value("${cloud.aws.end-point.uri}")
    private String sqsEndPoint;

    @Autowired
    private ObjectMapper objectMapper;

    public EmailStore push(InboundEmailMsg inboundEmailMsg) {

        EmailStore emailStore = new EmailStore();

        try {
            emailStore.setContent(objectMapper.writeValueAsString(inboundEmailMsg));
        } catch (JsonProcessingException e) {
            throw new EmailPayloadProcessingException("Invalid Payload " +
                    "submitted for queueing.", e);
        }
        emailStore.setEmailStatus(EmailStatus.PENDING);
        emailStore = emailStoreRepository.save(emailStore);

        queueMessagingTemplate.send(sqsEndPoint,
                MessageBuilder.withPayload(emailStore.getId()).build());

        emailStore.setEmailStatus(EmailStatus.ENQUEUED);
        emailStoreRepository.save(emailStore);

        return emailStore;
    }

    public EmailStore fetch(long refId) {

        return emailStoreRepository.findById(refId).orElseThrow(

                () -> new EmailRecordNotFoundException("No records " + "found" +
                        " for the Reference ID : " + refId));
    }

}
