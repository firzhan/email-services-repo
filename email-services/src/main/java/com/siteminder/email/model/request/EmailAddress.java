package com.siteminder.email.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailAddress {

    @Email(message = "email ID is not in a valid format.")
    private String id;

    @NotNull(message = "Recipient's name is a mandatory field.")
    private String name;
}