package ru.sbt.jschool.session3.problem1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 */
public class AccountServiceImpl implements AccountService {
    protected FraudMonitoring fraudMonitoring;
    private ArrayList<Account> accounts = new ArrayList<>();
    private HashSet<Long> payments = new HashSet<>();

    public AccountServiceImpl(FraudMonitoring fraudMonitoring) {
        this.fraudMonitoring = fraudMonitoring;
    }

    @Override public Result create(long clientID, long accountID, float initialBalance, Currency currency) {
        if (find(accountID) == null) {
            accounts.add(new Account(clientID, accountID, currency, initialBalance));
            return Result.OK;
        }
        return Result.ALREADY_EXISTS;
    }

    @Override public List<Account> findForClient(long clientID) {
        ArrayList<Account> result = new ArrayList<>();
        for (Account elem : accounts) {
            if (elem.getClientID() == clientID) {
                result.add(elem);
            }
        }
        return result;
    }

    @Override public Account find(long accountID) {
        for (Account elem : accounts) {
            if (elem.getClientID() == accountID) {
                return elem;
            }
        }
        return null;
    }

    @Override public Result doPayment(Payment payment) {
        if (payments.contains(payment.getOperationID())) {
            return Result.ALREADY_EXISTS;
        }
        if (payment.getAmount() > find(payment.getPayerAccountID()).getBalance()) {
            return Result.INSUFFICIENT_FUNDS;
        }
        if (find(payment.getPayerAccountID()) == null) {
            return Result.PAYER_NOT_FOUND;
        }
        if (find(payment.getRecipientAccountID()) != null) {
            return Result.RECIPIENT_NOT_FOUND;
        }
        if (fraudMonitoring.check(payment.getPayerID())) {
            return Result.FRAUD;
        }
        payments.add(payment.getOperationID());
        return Result.OK;
    }
}
