package com.noharacorp.noharacorpapp.Controller;

import com.noharacorp.noharacorpapp.Model.Account;
import com.noharacorp.noharacorpapp.Model.PaymentProfile;
import com.noharacorp.noharacorpapp.Model.User;
import com.noharacorp.noharacorpapp.Repository.AccountRepository;
import com.noharacorp.noharacorpapp.Repository.NotificationRepository;
import com.noharacorp.noharacorpapp.Repository.SearchRepository;
import com.noharacorp.noharacorpapp.Repository.TransactionRepository;
import com.noharacorp.noharacorpapp.Service.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ProfileController {

    @Autowired
    private UserDetailServiceImpl userDetailService;
    @Autowired
    SearchRepository searchRepository;
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    AccountRepository accountRepository;


    @RequestMapping(value = "/myprofile", method = RequestMethod.GET)
    public ModelAndView navigatetomyprofile(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userDetailService.findUserByEmail(authentication.getName());
        Account useraccount = searchRepository.searchCurrentUserAccount(user.getMobileno());
        modelAndView.addObject("user_details", user);
        modelAndView.addObject("account_details", useraccount);
        modelAndView.setViewName("myprofile");

        return modelAndView;
    }

    @RequestMapping(value = "/cav")
    public @ResponseBody
    ResponseEntity<String> checkAvailability(@RequestParam(value = "ppurl") String paymenturlname){
        PaymentProfile paymentProfile = searchRepository.searchProfileNames(paymenturlname);
        if(paymentProfile == null){
            return ResponseEntity.ok("Available");
        }
        System.out.println(paymenturlname);
        return ResponseEntity.ok("Not Available");
    }

    @RequestMapping(value = "/createpaymentprofile")
    public @ResponseBody
    ResponseEntity<String> createPaymentProfile(@RequestParam(value = "ppurl") String paymenturlname){
        PaymentProfile paymentProfile = searchRepository.searchProfileNames(paymenturlname);
        if(paymentProfile == null){
            return ResponseEntity.ok("Available");
        }
        System.out.println(paymenturlname);
        return ResponseEntity.ok("Not Available");
    }
}
