package com.siteminder.email.model.dto;

import com.siteminder.email.model.state.EmailStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "email_store")
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class EmailStore {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "email_content")
    private String content;

    @Enumerated
    @Column(name = "email_status")
    private EmailStatus emailStatus;
}
