package com.siteminder.email.service;

import com.siteminder.email.model.request.InboundEmailMsg;

/**
 * Interface that should be implemented by the new mail service clients
 */
@FunctionalInterface
public interface EmailServiceProviderClient {
    boolean sendEmail(InboundEmailMsg inboundEmailMsg,
                      String systemSenderAddress, String systemSenderName);
}
