package com.siteminder.email.repo;

import com.siteminder.email.model.dto.EmailStore;
import com.siteminder.email.model.state.EmailStatus;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class EmailStoreRepositoryTest {

    @Autowired
    private EmailStoreRepository emailStoreRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @BeforeEach
    public void setUp() {

        EmailStore emailStore = new EmailStore();
        emailStore.setEmailStatus(EmailStatus.ENQUEUED);
        emailStore.setContent("Email-Content");
        testEntityManager.persist(emailStore);
    }

    @AfterEach
    public void tearDown() {

        emailStoreRepository.deleteAll();
    }

    @Test
    public void EmailDataStore_WhenANewEntityIsPersistedAndRetrieved_ThenIsCorrect() {

        /*EmailStore emailStore = new EmailStore();
        emailStore.setEmailStatus(EmailStatus.ENQUEUED);
        emailStore.setContent("Email-Content");
        testEntityManager.persist(emailStore);*/

        Iterable<EmailStore> emailStoreIterable =
                emailStoreRepository.findAll();

        List<EmailStore> list = new ArrayList<>();
        emailStoreIterable.iterator().forEachRemaining(list::add);

        System.out.println("****************************************" + list.size());
        Assert.assertEquals(list.size(), 1);

        EmailStore emailStore = list.get(0);
        Assert.assertNotNull(emailStore);
        Assert.assertEquals(emailStore.getContent(), "Email-Content");
    }
}