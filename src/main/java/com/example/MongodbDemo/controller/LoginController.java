package com.example.MongodbDemo.controller;

import com.example.MongodbDemo.EmailService;
import com.example.MongodbDemo.GeneratePDFReport;
import com.example.MongodbDemo.document.Account;
import com.example.MongodbDemo.document.Transaction;
import com.example.MongodbDemo.document.User;
import com.example.MongodbDemo.repository.SearchRepository;
import com.example.MongodbDemo.repository.TransactionRepository;
import com.example.MongodbDemo.service.CustomUserDetailService;
import com.mongodb.MongoServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.MediaType;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Controller
public class LoginController {

    @Autowired
    private CustomUserDetailService userDetailService;
    @Autowired
    SearchRepository searchRepository;
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    private EmailService emailService;

    private static final DateFormat sdf = new SimpleDateFormat("dd/mm/yyyy HH:mm:ss");

    @RequestMapping(value = {"/","/login"}, method = RequestMethod.GET)
    public ModelAndView login(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        return modelAndView;
    }

    @RequestMapping(value = "/signup", method = RequestMethod.GET)
    public ModelAndView signup(){
        ModelAndView modelAndView = new ModelAndView();
        User user = new User();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("signup");
        return modelAndView;
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public ModelAndView createNewUser(@Valid User user, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        User userExists = userDetailService.findUserByEmail(user.getEmail());
        if(userExists != null){
            bindingResult.rejectValue("email", "error.user", "Already Registered");
        }
        if(bindingResult.hasErrors()){
            modelAndView.setViewName("signup");
        }else{
            userDetailService.saveUser(user);
            modelAndView.addObject("successMessage", "Successfully Registered");
            modelAndView.addObject("user", new User());
            modelAndView.setViewName("login");
        }
        return modelAndView;
    }

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public ModelAndView dashboard(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userDetailService.findUserByEmail(authentication.getName());
        modelAndView.addObject("currentUser", user);
        modelAndView.addObject("fullName", "Welcome "+user.getName());
        modelAndView.addObject("adminMessage", "Content Available");
        modelAndView.setViewName("dashboard");
        return modelAndView;
    }

    @RequestMapping(value = {"/home"}, method = RequestMethod.GET)
    public ModelAndView home(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");
        return modelAndView;
    }

    @RequestMapping(value = "paymentpage", method = RequestMethod.GET)
    public ModelAndView paymentPage(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("makepayment");
        return modelAndView;
    }

    @RequestMapping(value = "errorpayment", method = RequestMethod.GET)
    public ModelAndView errorPayment(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("errorpayment");
        return modelAndView;
    }

    @RequestMapping(value = "makepaymentaccountno", method = RequestMethod.POST)
    public ModelAndView makePaymentUsingAccountNo(@RequestParam("accountnumber") String accountno, @RequestParam("ifsc") String ifsc,
                                                  @RequestParam("transactionamount") String amount, @RequestParam("transactiondesc") String desc,
                                                @RequestParam("transactionPIN") String trans_pin){
        ModelAndView modelAndView = new ModelAndView();
        Account recipientaccount = searchRepository.searchAccounts(accountno);

        if(recipientaccount.getIfsc().equals(ifsc.toUpperCase())) {
            return processPayment(recipientaccount, amount, desc, modelAndView, trans_pin);
        }else{
            modelAndView.addObject("errorMessage", "Invalid IFSC Entered");
            modelAndView.setViewName("errorpayment");
        }

        return modelAndView;
    }

    @RequestMapping(value = "makepaymentuniqueid", method = RequestMethod.POST)
    public ModelAndView makePaymentusinguniqueid(@RequestParam("uniquepaymentid") String uniquepaymentid, @RequestParam("amount") String amount,
                                                 @RequestParam("transactiondesc") String desc, @RequestParam("transactionPIN") String trans_pin){
        ModelAndView modelAndView = new ModelAndView();
        Account recipientaccount = searchRepository.searchAccountsUsingPaymentID(uniquepaymentid);
        return processPayment(recipientaccount, amount, desc, modelAndView, trans_pin);
    }

    private ModelAndView processPayment(Account recipientaccount, String amount, String desc, ModelAndView modelAndView, String transactionpin){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userDetailService.findUserByEmail(authentication.getName());
        Account useraccount = searchRepository.searchCurrentUserAccount(user.getMobileno());
        User recipient =searchRepository.findUserFromMobileNo(recipientaccount.getMobileno());

        try {
            if(recipientaccount != null) {
                if (Integer.parseInt(useraccount.getBalance()) > Integer.parseInt(amount) && transactionpin.equals(user.getTransactionpin())) {
                    transactionRepository.save(new Transaction("Transaction: NOHARA" + recipientaccount.getAccountnumber(), desc,
                            amount, "Shopping", recipientaccount.getAccountnumber(), useraccount.getAccountnumber(),
                            sdf.format(new Date()), "12312313123"));
                    searchRepository.updateAccountBalances(useraccount.getAccountnumber(), recipientaccount.getAccountnumber(), amount);
                    modelAndView.addObject("recipientname", recipient.getName());
                    modelAndView.addObject("amount", amount);
                    if(!recipientaccount.getUnique_paymentid().equals("")){
                        modelAndView.addObject("accountnumber", recipientaccount.getUnique_paymentid());
                    }else {
                        modelAndView.addObject("accountnumber", recipientaccount.getAccountnumber());
                    }
                    modelAndView.addObject("usermobile", user.getMobileno());
                    modelAndView.addObject("transaction_number", "12312313123");
                    modelAndView.setViewName("paymentconfirmation");
                } else {
                    modelAndView.addObject("errorMessage", "No Sufficient Balance for Transaction");
                    modelAndView.setViewName("errorpayment");
                }
            } else {
                modelAndView.addObject("errorMessage", "Account or User Not Found");
                modelAndView.setViewName("errorpayment");
            }
        } catch (MongoServerException mongoserverexception){
            modelAndView.addObject("errorMessage", "Transaction Failed!!!");
            modelAndView.setViewName("errorpayment");
        }
        return modelAndView;
    }

    @RequestMapping(value = "/sendmail", method = RequestMethod.POST)
    public ModelAndView sendMail(){
        ModelAndView modelAndView = new ModelAndView();
        emailService.sendSimpleMessage("This is a Random Message", "shirvandkar.prasad@gmail.com");
        modelAndView.setViewName("mailsend");
        System.out.println("Executed");
        return modelAndView;
    }

    @RequestMapping(value = "/pdfreport", method = RequestMethod.GET, produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> transactionReport() throws IOException {

        List<String> list = new ArrayList<>();
        list.add("Prasad");
        list.add("Shirvandakr");
        list.add("Lenovo");
        list.add("Ideapad");
        list.add("520");

        ByteArrayInputStream bis = GeneratePDFReport.citiesReport(list);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=listnames.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));

    }


}
