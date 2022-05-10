package com.example.MongodbDemo.repository;

import com.example.MongodbDemo.document.Account;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountRepository extends MongoRepository<Account, String> {
}
