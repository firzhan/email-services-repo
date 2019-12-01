package com.siteminder.email.model.request;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class InboundEmailMsg {

    @NotNull(message = "To email address is a mandatory field.")
    private List<EmailAddress> to;

    private List<EmailAddress> cc;

    private List<EmailAddress> bcc;

    @NotBlank(message = "Subject is a mandatory field.")
    private String subject;
    @NotBlank(message = "Content is a mandatory field.")
    private String content;
}
