package com.example.MongodbDemo.repository;

import com.example.MongodbDemo.document.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

    User findByEmail(String email);

}
