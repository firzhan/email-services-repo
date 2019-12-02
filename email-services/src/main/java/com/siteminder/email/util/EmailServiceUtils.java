package com.siteminder.email.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siteminder.email.model.request.EmailAddress;
import com.siteminder.email.model.request.InboundEmailMsg;
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

public final class EmailServiceUtils {

    private EmailServiceUtils() {

    }

    public static boolean isValidEmail(InboundEmailMsg inboundEmailMsg) {

        if (inboundEmailMsg.getTo() == null && inboundEmailMsg.getCc() == null && inboundEmailMsg.getBcc() == null) {
            return false;
        }

        if (inboundEmailMsg.getTo().size() == 0 && inboundEmailMsg.getCc().size() == 0 && inboundEmailMsg.getBcc().size() == 0) {
            return false;
        }

/*
        return isValidateEmailList(inboundEmailMsg.getTo()) && isValidateEmailList(inboundEmailMsg.getCc()) && isValidateEmailList(inboundEmailMsg.getBcc());
*/
        return true;
    }

    public static String generateAuthHeaderValue(String username,
                                                 String password){
        String auth = username + ":" + password;
        byte[] encodedAuth =
                Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        return  "Basic " + new String(encodedAuth);
    }

   /* public static String decodeBase64(String encoded) {

        return new String(Base64.getDecoder().decode(encoded));
    }
*/
    private static boolean isValidateEmailList(List<EmailAddress> emailList) {

        if (emailList != null) {
            for (EmailAddress email : emailList) {
                if (!Pattern.compile(EmailServiceConstant.EMAIL_FORMAT_REGEX).matcher(email.getId()).matches()) {
                    return false;
                }
            }
        }

        return true;
    }

    public static String rawJsonToPrettyPrint(String json) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writerWithDefaultPrettyPrinter().
                writeValueAsString(objectMapper.readValue(json, Object.class));
    }
}
