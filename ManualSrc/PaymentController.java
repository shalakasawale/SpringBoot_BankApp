package com.noharacorp.noharacorpapp.Controller;

import com.mongodb.MongoServerException;
import com.noharacorp.noharacorpapp.Config.ProtectedConfigFile;
import com.noharacorp.noharacorpapp.Model.Account;
import com.noharacorp.noharacorpapp.Model.Notification;
import com.noharacorp.noharacorpapp.Model.Transaction;
import com.noharacorp.noharacorpapp.Model.User;
import com.noharacorp.noharacorpapp.Repository.NotificationRepository;
import com.noharacorp.noharacorpapp.Repository.SearchRepository;
import com.noharacorp.noharacorpapp.Repository.TransactionRepository;
import com.noharacorp.noharacorpapp.Service.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class PaymentController {

    @Autowired
    private UserDetailServiceImpl userDetailService;
    @Autowired
    SearchRepository searchRepository;
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    NotificationRepository notificationRepository;

    private static final DateFormat sdf = new SimpleDateFormat("dd MMMM, yyyy 'at' hh:mm:ss a");

    @RequestMapping(value = "paymentpage", method = RequestMethod.GET)
    public ModelAndView paymentPage(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("payment_type","Make Payment");
        modelAndView.setViewName("makepayment");
        return modelAndView;
    }

    @RequestMapping(value = "requestpaymentpage", method = RequestMethod.GET)
    public ModelAndView requestPaymentPage(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("payment_type","Request Payment");
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
                                                  @RequestParam("transactionPIN") String trans_pin, @RequestParam("transactiontype") String transactiontype) throws GeneralSecurityException, IOException {
        Account recipientaccount = searchRepository.searchAccounts(accountno);
        if(ifsc.equals(recipientaccount.getIfsc())) {
            return processPayment(recipientaccount, amount, desc, trans_pin, 1, transactiontype);
        }else{
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("errorMessage", "Incorrect IFSC");
            modelAndView.setViewName("errorpayment");
            return modelAndView;
        }
    }

    @RequestMapping(value = "makepaymentuniqueid", method = RequestMethod.POST)
    public ModelAndView makePaymentusinguniqueid(@RequestParam("uniquepaymentid") String uniquepaymentid, @RequestParam("amount") String amount,
                                                 @RequestParam("transactiondesc") String desc, @RequestParam("transactionPIN") String trans_pin,
                                                 @RequestParam("transactiontype") String transactiontype) throws GeneralSecurityException, IOException {
        Account recipientaccount = searchRepository.searchAccountsUsingPaymentID(uniquepaymentid);
        return processPayment(recipientaccount, amount, desc, trans_pin, 0, transactiontype);
    }

    private ModelAndView processPayment(Account recipientaccount, String amount, String desc, String transactionpin, int paymentusingaccountno,
                                        String transactiontype) throws GeneralSecurityException, IOException {
        ModelAndView modelAndView = new ModelAndView();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userDetailService.findUserByEmail(authentication.getName());
        Account useraccount = searchRepository.searchCurrentUserAccount(user.getMobileno());
        User recipient =searchRepository.findUserFromMobileNo(recipientaccount.getMobileno());
        int newTransactionNumber = searchRepository.getTransactionSequenceNumber();

        try {
            if(recipientaccount != null) {
                if (transactiontype.equals("request")) {
                    transactionRepository.save(new Transaction(desc,
                            amount, "Shopping", useraccount.getAccountnumber(), recipientaccount.getAccountnumber(),
                            sdf.format(new Date()), String.valueOf(newTransactionNumber), "request",
                            useraccount.getBalance(), recipientaccount.getBalance()));
                    notificationRepository.save(new Notification(String.valueOf(newTransactionNumber), recipientaccount.getAccountnumber(), "Paymeny Request from "+user.getName(),
                            user.getName()+" has requested Rs."+ amount+" for "+desc, "request", "active", sdf.format(new Date())));
                    modelAndView.setViewName("requestsuccessful");
                    return modelAndView;
                } else {
                    if ((Integer.parseInt(useraccount.getBalance()) > Integer.parseInt(amount)) && (transactionpin.equals(new ProtectedConfigFile().decrypt(user.getTransactionpin())))) {
                        int update_balance_sender = Integer.parseInt(useraccount.getBalance()) - Integer.parseInt(amount);
                        int update_balance_recipient = Integer.parseInt(recipientaccount.getBalance()) + Integer.parseInt(amount);

                        transactionRepository.save(new Transaction(desc,
                                amount, "Shopping", useraccount.getAccountnumber(), recipientaccount.getAccountnumber(),
                                sdf.format(new Date()), String.valueOf(newTransactionNumber), "payment",
                                String.valueOf(update_balance_sender), String.valueOf(update_balance_recipient)));

                        searchRepository.updateAccountBalances(useraccount.getAccountnumber(), recipientaccount.getAccountnumber(), amount,
                                update_balance_sender, update_balance_recipient);

                        notificationRepository.save(new Notification(String.valueOf(newTransactionNumber), recipientaccount.getAccountnumber(), "Payment Received from "+user.getName(),
                                user.getName()+" has sent you Rs."+amount+" for "+desc, "payment", "active", sdf.format(new Date())));

                        modelAndView.addObject("recipientname", recipient.getName());
                        modelAndView.addObject("amount", amount);
                        if (paymentusingaccountno == 0) {
                            modelAndView.addObject("accountnumber", recipientaccount.getUnique_paymentid());
                        } else {
                            modelAndView.addObject("accountnumber", recipientaccount.getAccountnumber());
                        }
                        modelAndView.addObject("usermobile", user.getMobileno());
                        modelAndView.addObject("transaction_number", String.valueOf(newTransactionNumber));
                        modelAndView.setViewName("paymentconfirmation");
                        return modelAndView;
                    } else {
                        modelAndView.addObject("errorMessage", "No Sufficient Balance for Transaction");
                        modelAndView.setViewName("errorpayment");
                        return modelAndView;
                    }
                }
            } else {
                modelAndView.addObject("errorMessage", "Account or User Not Found");
                modelAndView.setViewName("errorpayment");
                return modelAndView;
            }
        } catch (MongoServerException mongoserverexception){
            modelAndView.addObject("errorMessage", "Transaction Failed!!!");
            modelAndView.setViewName("errorpayment");
            return modelAndView;
        }
    }
}
