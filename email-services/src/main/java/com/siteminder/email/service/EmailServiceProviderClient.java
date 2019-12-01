package com.siteminder.email.service;

import com.siteminder.email.model.request.InboundEmailMsg;

@FunctionalInterface
public interface EmailServiceProviderClient {
    boolean sendEmail(InboundEmailMsg inboundEmailMsg,
                      String systemSenderAddress, String systemSenderName);
}
