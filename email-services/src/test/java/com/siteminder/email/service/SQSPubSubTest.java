package com.siteminder.email.service;


import com.siteminder.email.model.request.EmailAddress;
import com.siteminder.email.model.request.InboundEmailMsg;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SQSPubSubTest {

    @Test
    void EmailStoreRefID_Published_And_Fetched_Correctly() {

        EmailPersistingService emailPersistingService =
                mock(EmailPersistingService.class);

        InboundEmailMsg inboundEmailMsg = new InboundEmailMsg();

        EmailAddress emailAddress = new EmailAddress();
        emailAddress.setId("firzhan007@gmail.com");
        emailAddress.setName("firzhan");

        List<EmailAddress> emailAddresses = new ArrayList<>();
        emailAddresses.add(emailAddress);
        inboundEmailMsg.setTo(emailAddresses);

        inboundEmailMsg.setContent("Hello");
        inboundEmailMsg.setSubject("Hello");

        emailPersistingService.push(inboundEmailMsg);

        verify(emailPersistingService, times(1)).push(inboundEmailMsg);

    }

}
