package com.siteminder.email.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMsg {

    private String message;
    @JsonProperty("http-status-code")
    private int httpStatusCode;

   /* public ResponseMsg(String message, int httpStatusCode) {
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
*/
/*    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getMessage() {
        return message;
    }*/
}
