package com.example.MongodbDemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void sendSimpleMessage(String text, String to){

        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("Random Subject");
        message.setText(text);
        message.setTo(to);

        emailSender.send(message);
    }

}