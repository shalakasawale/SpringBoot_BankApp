package com.example.MongodbDemo.controller;

import com.example.MongodbDemo.document.Account;
import com.example.MongodbDemo.document.User;
import com.example.MongodbDemo.repository.SearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/publicpage")
public class PublicPageController {

    @Autowired
    SearchRepository searchRepository;

    @RequestMapping(value = "/{publicpageid}", method = RequestMethod.GET)
    public ModelAndView getDynamicUrlValue(@PathVariable String publicpageid) {
        User user = searchRepository.searchUserByPaymentProfile(publicpageid);
        Account account = searchRepository.searchCurrentUserAccount(user.getMobileno());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name", user.getName());
        modelAndView.addObject("account", account.getAccountnumber());
        return modelAndView;
    }

}
