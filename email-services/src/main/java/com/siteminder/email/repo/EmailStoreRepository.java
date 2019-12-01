package com.siteminder.email.repo;

import com.siteminder.email.model.dto.EmailStore;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailStoreRepository extends CrudRepository<EmailStore, Long> {

}
