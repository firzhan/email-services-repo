package com.siteminder.email.controller;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.siteminder.email.exception.DataPersistenceException;
import com.siteminder.email.exception.EmailPayloadProcessingException;
import com.siteminder.email.model.dto.EmailStore;
import com.siteminder.email.model.request.InboundEmailMsg;
import com.siteminder.email.model.response.EmailStatusResponseMessage;
import com.siteminder.email.model.response.PubResponseMsg;
import com.siteminder.email.model.response.ResponseMsg;
import com.siteminder.email.model.state.EmailStatus;
import com.siteminder.email.service.EmailPersistingService;
import com.siteminder.email.util.EmailServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Slf4j
@RestController
@RequestMapping("/email")
public class EmailServiceController {

    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;

    @Autowired
    private EmailPersistingService emailPersistingService;

    @Value("${cloud.aws.end-point.uri}")
    private String sqsEndPoint;

    private boolean isDebugEnabled = log.isDebugEnabled();

    @PostMapping(value = "/submit", produces =
            MediaType.APPLICATION_JSON_VALUE, consumes =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseMsg> submit(@Valid @RequestBody InboundEmailMsg inboundEmailMsg, Errors errors) {

        if(isDebugEnabled)
            log.debug("Incoming Payload for submission : " + inboundEmailMsg.toString());

        if (errors.hasErrors()) {
            throw new EmailPayloadProcessingException(errors.getAllErrors().get(0).getDefaultMessage());
        }

        //This validation part is eliminated by introducing javax.validation
     /*   if (!EmailServiceUtils.isValidEmail(inboundEmailMsg)) {
            throw new EmailPayloadProcessingException("Email address should " +
                    "be in the correct format.");

        }*/

        EmailStore emailStore =
                emailPersistingService.push(inboundEmailMsg);

        if(emailStore == null || emailStore.getEmailStatus() != EmailStatus.ENQUEUED){
            throw new DataPersistenceException("Email Service Couldn't " +
                    "enqueue the request");
        }
        return ResponseEntity.status(HttpStatus.OK).body(new PubResponseMsg("Email correctly enqueued.",
                emailStore.getId(), HttpStatus.OK.value(), emailStore.getEmailStatus().name()));
    }

    @GetMapping(value = "/status/{storeRef}", produces =
            MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmailStatusResponseMessage> getStatus(@PathVariable("storeRef") @NotBlank String storeRef) {

        try {
            EmailStore emailStore =
                    emailPersistingService.fetch(Long.parseLong(storeRef));
            EmailStatusResponseMessage emailStatusResponseMessage =
                    new EmailStatusResponseMessage("Email Status at the time "
                            + ZonedDateTime.now(ZoneId.of("Australia/ACT")).toString(), Long.parseLong(storeRef), HttpStatus.OK.value(), emailStore.getEmailStatus().name(), EmailServiceUtils.rawJsonToPrettyPrint(emailStore.getContent()));
            return ResponseEntity.status(HttpStatus.OK).body(emailStatusResponseMessage);
        } catch (JsonProcessingException exception) {
            throw new DataPersistenceException("Processing of " + "json input" +
                    " message content failed due to Json Error ", exception);
        } catch (NumberFormatException exception) {
            throw new EmailPayloadProcessingException("Email Reference Id is "
                    + "not a number", exception);
        }
    }
}
