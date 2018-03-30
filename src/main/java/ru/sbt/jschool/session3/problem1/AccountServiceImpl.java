package ru.sbt.jschool.session3.problem1;

import java.util.*;

/**
 */
public class AccountServiceImpl implements AccountService {
    protected FraudMonitoring fraudMonitoring;
    private Map<Long, List<Account>> clientIdToAccountsList = new HashMap<>();
    private Map<Long, Account> accountIdToAccount = new HashMap<>();
    private Set<Long> payments = new HashSet<>();

    public AccountServiceImpl(FraudMonitoring fraudMonitoring) {
        this.fraudMonitoring = fraudMonitoring;
    }

    @Override public Result create(long clientID, long accountID, float initialBalance, Currency currency) {
        if (fraudMonitoring.check(clientID)) {
            return Result.FRAUD;
        }
        if (find(accountID) == null) {
            Account account = new Account(clientID, accountID, currency, initialBalance);
            accountIdToAccount.put(accountID, account);
            if (findForClient(clientID) == null) {
                List<Account> accounts = new ArrayList<>();
                accounts.add(account);
                clientIdToAccountsList.put(clientID, accounts);
            } else {
                clientIdToAccountsList.get(clientID).add(account);
            }
            return Result.OK;
        }
        return Result.ALREADY_EXISTS;
    }

    @Override public List<Account> findForClient(long clientID) {
        return clientIdToAccountsList.get(clientID);
    }

    @Override public Account find(long accountID) {
        return accountIdToAccount.get(accountID);
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
        if (find(payment.getPayerAccountID()) == null || !isPayerIdExists(payment.getPayerID())) {
            return Result.PAYER_NOT_FOUND;
        }
        if (find(payment.getRecipientAccountID()) == null || !isPayerIdExists(payment.getRecipientID())) {
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
        return payments.contains(payerID);
    }
}
