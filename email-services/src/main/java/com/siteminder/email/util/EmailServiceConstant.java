package com.siteminder.email.util;

public final class EmailServiceConstant {

    private EmailServiceConstant() {}

    public static String EMAIL_FORMAT_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\."+
            "[a-zA-Z0-9_+&*-]+)*@" +
            "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
            "A-Z]{2,7}$";
}
