package com.example.MongodbDemo.repository;

import com.example.MongodbDemo.document.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
}
