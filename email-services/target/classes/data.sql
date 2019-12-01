DROP TABLE IF EXISTS email_store;

CREATE TABLE email_store (
            id LONG AUTO_INCREMENT,
            email_content VARCHAR(4000) NOT NULL,
            email_status INT NOT NULL DEFAULT 0,
            PRIMARY KEY (id)
);