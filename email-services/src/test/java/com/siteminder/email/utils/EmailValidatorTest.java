package com.siteminder.email.utils;

import com.siteminder.email.model.request.EmailAddress;
import com.siteminder.email.model.request.InboundEmailMsg;
import com.siteminder.email.util.EmailServiceUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class EmailValidatorTest {

    @Test
    public void GivenEmptyEmail_WhenValidated_ThenIsInvalid() {

        InboundEmailMsg inboundEmailMsg = new InboundEmailMsg();
        EmailAddress to = new EmailAddress("", "test2");
        List<EmailAddress> toList = new ArrayList<>();
        toList.add(to);
        inboundEmailMsg.setTo(toList);

        Assert.assertEquals(EmailServiceUtils.isValidEmail(inboundEmailMsg), false);
    }

    @Test
    public void GivenValidEmail_WhenValidated_ThenIsValid() {

        InboundEmailMsg inboundEmailMsg = new InboundEmailMsg();
        EmailAddress to = new EmailAddress("abc@correct.com", "test2");
        List<EmailAddress> toList = new ArrayList<>();
        toList.add(to);
        inboundEmailMsg.setTo(toList);

        Assert.assertEquals(EmailServiceUtils.isValidEmail(inboundEmailMsg),
                true);
    }

    @Test
    public void GivenNullEmail_WhenValidated_ThenIsInvalid() {
        InboundEmailMsg inboundEmailMsg = new InboundEmailMsg();
        Assert.assertEquals(EmailServiceUtils.isValidEmail(inboundEmailMsg), false);
    }

    @Test
    public void GivenInvalidEmails_WhenValidated_ThenAreInvalid() {
        InboundEmailMsg inboundEmailMsg = new InboundEmailMsg();
        EmailAddress to = new EmailAddress("*********", "test2");
        List<EmailAddress> toList = new ArrayList<>();
        toList.add(to);
        inboundEmailMsg.setTo(toList);
        Assert.assertEquals(EmailServiceUtils.isValidEmail(inboundEmailMsg), false);

        toList.clear();

        to = new EmailAddress("abc@gm", "test2");
        toList.add(to);
        inboundEmailMsg.setTo(toList);
        Assert.assertEquals(EmailServiceUtils.isValidEmail(inboundEmailMsg), false);
    }

}
