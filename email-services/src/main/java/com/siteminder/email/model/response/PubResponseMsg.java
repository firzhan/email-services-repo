package com.siteminder.email.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PubResponseMsg extends ResponseMsg {

    @JsonProperty("email-store-id")
    private long emailStoreId;
    @JsonProperty("email-status")
    private String emailStatus;

    public PubResponseMsg(String message, long emailStoreId,
                          int httpStatusCode, String emailStatus) {
        super(message,httpStatusCode);
        this.emailStoreId = emailStoreId;
        this.emailStatus = emailStatus;
    }

}
