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
        if (fraudMonitoring.check(clientID)) {
            return Result.FRAUD;
        }
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
            if (elem.getAccountID() == accountID) {
                return elem;
            }
        }
        return null;
    }

    @Override public Result doPayment(Payment payment) {
        if (fraudMonitoring.check(payment.getPayerID())) {
            return Result.FRAUD;
        }
        if (payments.contains(payment.getOperationID())) {
            return Result.ALREADY_EXISTS;
        }
        if (find(payment.getPayerAccountID()) != null && (payment.getAmount() > find(payment.getPayerAccountID()).getBalance())) {
            return Result.INSUFFICIENT_FUNDS;
        }
        if (!isPayerIdExists(payment.getPayerID())) {
            return Result.PAYER_NOT_FOUND;
        }
        if (find(payment.getPayerAccountID()) == null) {
            return Result.PAYER_NOT_FOUND;
        }
        if (find(payment.getRecipientAccountID()) == null) {
            return Result.RECIPIENT_NOT_FOUND;
        }
        if (!isPayerIdExists(payment.getRecipientID())) {
            return Result.RECIPIENT_NOT_FOUND;
        }
        payments.add(payment.getOperationID());
        Account from = find(payment.getPayerAccountID());
        Account to = find(payment.getRecipientAccountID());
        float sum = payment.getAmount();
        if (from.getCurrency() != to.getCurrency()) {
            sum = from.getCurrency().to(payment.getAmount(), to.getCurrency());
        }
        from.setBalance(from.getBalance() - payment.getAmount());
        to.setBalance(to.getBalance() + sum);
        return Result.OK;
    }

    private boolean isPayerIdExists(long payerID) {
        for (Account elem : accounts) {
            if (elem.getClientID() == payerID) return true;
        }
        return false;
    }
}
