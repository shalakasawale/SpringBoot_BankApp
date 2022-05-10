package com.example.MongodbDemo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "transaction")
public class Transaction {

    @Id
    private String transaction_id;
    @Indexed(unique = true, direction = IndexDirection.DESCENDING, dropDups = true)
    private String transaction_name;
    private String transaction_desc;
    private String transaction_amount;
    private String transaction_tag;
    private String transaction_from_account;
    private String transaction_to_account;
    private String transaction_date;
    private String transaction_number;

    public Transaction(String transaction_name, String transaction_desc, String transaction_amount, String transaction_tag,
                       String transaction_from_account, String transaction_to_account, String transaction_date, String transaction_number){
        this.transaction_name = transaction_name;
        this.transaction_desc = transaction_desc;
        this.transaction_amount = transaction_amount;
        this.transaction_tag = transaction_tag;
        this.transaction_from_account = transaction_from_account;
        this.transaction_to_account = transaction_to_account;
        this.transaction_date = transaction_date;
        this.transaction_number = transaction_number;
    }

    public String getTransaction_number() {
        return transaction_number;
    }

    public void setTransaction_number(String transaction_number) {
        this.transaction_number = transaction_number;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public String getTransaction_name() {
        return transaction_name;
    }

    public void setTransaction_name(String transaction_name) {
        this.transaction_name = transaction_name;
    }

    public String getTransaction_desc() {
        return transaction_desc;
    }

    public void setTransaction_desc(String transaction_desc) {
        this.transaction_desc = transaction_desc;
    }

    public String getTransaction_amount() {
        return transaction_amount;
    }

    public void setTransaction_amount(String transaction_amount) {
        this.transaction_amount = transaction_amount;
    }

    public String getTransaction_tag() {
        return transaction_tag;
    }

    public void setTransaction_tag(String transaction_tag) {
        this.transaction_tag = transaction_tag;
    }

    public String getTransaction_from_account() {
        return transaction_from_account;
    }

    public void setTransaction_from_account(String transaction_from_account) {
        this.transaction_from_account = transaction_from_account;
    }

    public String getTransaction_to_account() {
        return transaction_to_account;
    }

    public void setTransaction_to_account(String transaction_to_account) {
        this.transaction_to_account = transaction_to_account;
    }

    public String getTransaction_date() {
        return transaction_date;
    }

    public void setTransaction_date(String transaction_date) {
        this.transaction_date = transaction_date;
    }
}
