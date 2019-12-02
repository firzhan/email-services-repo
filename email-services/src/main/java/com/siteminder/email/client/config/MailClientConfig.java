package com.siteminder.email.client.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Setter
@Getter
public class MailClientConfig {

    @Value( "${email-service-provider.mail-gun.uri}" )
    private String mailGunURI;

    @Value( "${email-service-provider.mail-gun.username}" )
    private String mailGunUsername;

    @Value( "${email-service-provider.mail-gun.password}" )
    private String mailGunPassword;

    @Value( "${email-service-provider.spark-post.uri}" )
    private String sparkPostURI;

    @Value( "${email-service-provider.spark-post.authorization-code}" )
    private String sparkAuthCode;
}
