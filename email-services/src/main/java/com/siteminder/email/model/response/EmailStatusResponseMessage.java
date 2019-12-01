package com.siteminder.email.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailStatusResponseMessage extends PubResponseMsg {

    @JsonProperty("original-request")
    private String payload;

    public EmailStatusResponseMessage(String message, long emailStoreId,
                                      int httpStatusCode, String emailStatus,
                                      String payload){
        super(message, emailStoreId, httpStatusCode, emailStatus);
        this.payload = payload;
    }
}
