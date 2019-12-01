package com.siteminder.email.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailAddress {

    @NotNull(message = "email ID is a mandatory field.")
    private String id;

    @NotNull(message = "Recipient's name is a mandatory field.")
    private String name;

    /*EmailAddress() {}*/

    /*public EmailAddress(String id) {
        this.id = id;
    }

    public EmailAddress(String id, String name) {
        this(id);

        this.name = name;
    }
*/
    /*public String getId() {
        return  id;
    }

    public String getName() {
        return  name;
    }

    public Boolean hasName() {
        return (name != null && !name.trim().isEmpty());
    }*/

}