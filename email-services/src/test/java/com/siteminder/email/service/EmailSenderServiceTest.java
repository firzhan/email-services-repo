package com.siteminder.email.service;

import static org.junit.jupiter.api.Assertions.*;

import com.siteminder.email.client.MailGunServiceProviderClient;
import com.siteminder.email.client.SparkPostServiceProviderClient;
import com.siteminder.email.exception.EmailClientNotAvailableException;
import com.siteminder.email.model.dto.EmailStore;
import com.siteminder.email.model.request.InboundEmailMsg;
import com.siteminder.email.model.state.EmailStatus;
import com.siteminder.email.repo.EmailStoreRepository;
import com.siteminder.email.utils.TestUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class EmailSenderServiceTest {

    @InjectMocks
    EmailSenderService emailSenderService;

    @Mock
    private EmailStoreRepository emailStoreRepository;

    @Test
    public void GivenEmailSenderServiceWithNoClients_WhenSendEmail_ThenReturnsFalse() {

        EmailSenderService emailSenderService =
                new EmailSenderService(TestUtils.defaultCircuitBreakerRegistry(), TestUtils.defaultRetryRegistry(), emailStoreRepository);

        Assert.assertFalse(emailSenderService.send(1001L,
                TestUtils.dummyEmailMessage()));
    }

    @Test
    public void GivenEmailSenderServiceWithANotAvailableClient_WhenSendEmail_ThenReturnsFalse() {

        MailGunServiceProviderClient mailGunServiceProviderClient =
                mock(MailGunServiceProviderClient.class);

        lenient().doThrow(EmailClientNotAvailableException.class).when(mailGunServiceProviderClient).sendEmail(any(InboundEmailMsg.class), any(String.class), any(String.class));

        EmailSenderService emailSenderService =
                new EmailSenderService(TestUtils.defaultCircuitBreakerRegistry(), TestUtils.defaultRetryRegistry(), emailStoreRepository);
        emailSenderService.addEmailClient(mailGunServiceProviderClient);

        Assert.assertFalse(emailSenderService.send(1001L,
                TestUtils.dummyEmailMessage()));
    }

    @Test
    public void GivenEmailSenderServiceWithANotAvailableClientAndAnAvailableClient_WhenSendEmail_ThenReturnsTrue() {

        MailGunServiceProviderClient mailGunServiceProviderClient =
                mock(MailGunServiceProviderClient.class);

        lenient().when(mailGunServiceProviderClient.sendEmail(any(InboundEmailMsg.class), any(), any())).thenReturn(true);

        SparkPostServiceProviderClient sparkPostServiceProviderClient =
                mock(SparkPostServiceProviderClient.class);

        lenient().when(sparkPostServiceProviderClient.sendEmail(any(InboundEmailMsg.class), any(), any())).thenReturn(false);

        EmailSenderService emailSenderService =
                new EmailSenderService(TestUtils.defaultCircuitBreakerRegistry(), TestUtils.defaultRetryRegistry(), emailStoreRepository);

        EmailStore emailStore = new EmailStore();
        emailStore.setEmailStatus(EmailStatus.ENQUEUED);
        emailStore.setContent("Email-Content");
        emailStore.setId(1001);

        lenient().when(emailStoreRepository.findById(any(Long.class))).thenReturn(Optional.of(emailStore));

        emailSenderService.addEmailClient(mailGunServiceProviderClient);
        emailSenderService.addEmailClient(sparkPostServiceProviderClient);

        Assert.assertTrue(emailSenderService.send(1001L,
                TestUtils.dummyEmailMessage()));
    }

    @Test
    public void GivenEmailSenderServiceWithTwoNotAvailableClients_WhenSendEmail_ThenReturnsFalse() {

        MailGunServiceProviderClient mailGunServiceProviderClient =
                mock(MailGunServiceProviderClient.class);

        lenient().when(mailGunServiceProviderClient.sendEmail(any(InboundEmailMsg.class), anyObject(), anyObject())).
                thenThrow(EmailClientNotAvailableException.class);

        SparkPostServiceProviderClient sparkPostServiceProviderClient =
                mock(SparkPostServiceProviderClient.class);
        lenient().when(sparkPostServiceProviderClient.sendEmail(any(InboundEmailMsg.class), anyObject(), anyObject())).
                thenThrow(EmailClientNotAvailableException.class);

        EmailSenderService emailSenderService =
                new EmailSenderService(TestUtils.defaultCircuitBreakerRegistry(), TestUtils.defaultRetryRegistry(), emailStoreRepository);

        EmailStore emailStore = new EmailStore();
        emailStore.setEmailStatus(EmailStatus.ENQUEUED);
        emailStore.setContent("Email-Content");
        emailStore.setId(1001);

        lenient().when(emailStoreRepository.findById(any(Long.class))).thenReturn(Optional.of(emailStore));

        emailSenderService.addEmailClient(mailGunServiceProviderClient);
        emailSenderService.addEmailClient(sparkPostServiceProviderClient);

        Assert.assertFalse(emailSenderService.send(1001L,
                TestUtils.dummyEmailMessage()));
    }

}