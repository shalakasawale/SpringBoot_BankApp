package com.example.MongodbDemo.repository;

import com.example.MongodbDemo.document.Account;
import com.example.MongodbDemo.document.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public class SearchRepository {

    @Autowired
    MongoTemplate mongoTemplate;

    public User searchUserByPaymentProfile(String profilepageurl){
        return mongoTemplate.findOne(Query.query(Criteria.where("profilepageurl")
                .regex(profilepageurl, "i")), User.class);
    }

    public Account searchAccounts(String accountNumber){
        return mongoTemplate.findOne(Query.query(Criteria.where("accountnumber")
                .regex(accountNumber, "i")), Account.class);
    }

    public Account searchCurrentUserAccount(String mobilenumber){
        return mongoTemplate.findOne(Query.query(Criteria.where("mobileno")
                .regex(mobilenumber, "i")), Account.class);
    }

    public void updateAccountBalances(String accountno_one, String accountno_two, String amount){
        Account account_one = mongoTemplate.findOne(Query.query(Criteria.where("accountnumber")
                .regex(accountno_one, "i")), Account.class);
        Account account_two = mongoTemplate.findOne(Query.query(Criteria.where("accountnumber")
                .regex(accountno_two, "i")), Account.class);
        int account_one_balance = Integer.parseInt(account_one.getBalance()) - Integer.parseInt(amount);
        int account_two_balance = Integer.parseInt(account_two.getBalance()) + Integer.parseInt(amount);
        account_one.setBalance(String.valueOf(account_one_balance));
        account_two.setBalance(String.valueOf(account_two_balance));
        mongoTemplate.save(account_one);
        mongoTemplate.save(account_two);
    }

    public Account searchAccountsUsingPaymentID(String uniquePaymentID){
        return mongoTemplate.findOne(Query.query(Criteria.where("unique_paymentid")
                .regex(uniquePaymentID, "i")), Account.class);
    }

    public User findUserFromMobileNo(String mobileno){
        return mongoTemplate.findOne(Query.query(Criteria.where("mobileno")
                .regex(mobileno, "i")), User.class);
    }

}
