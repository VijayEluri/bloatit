package com.bloatit.framework;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;

import com.bloatit.common.PageIterable;
import com.bloatit.framework.lists.BankTransactionList;
import com.bloatit.model.data.DaoActor;
import com.bloatit.model.data.DaoBankTransaction;
import com.bloatit.model.data.DaoBankTransaction.State;

@Entity
public final class BankTransaction extends Identifiable {

    private final DaoBankTransaction dao;

    public static BankTransaction create(DaoBankTransaction daoBankTransaction) {
        return new BankTransaction(daoBankTransaction);
    }

    public static PageIterable<BankTransaction> getAllTransactionsOf(Actor author) {
        return new BankTransactionList(DaoBankTransaction.getAllTransactionsOf(author.getDao()));
    }

    public static BankTransaction getByToken(String token) {
        return create(DaoBankTransaction.getByToken(token));
    }

    public BankTransaction(String message, String token, DaoActor author, BigDecimal value, String orderReference) {
        this.dao = DaoBankTransaction.createAndPersist(message, token, author, value, orderReference);
    }

    private BankTransaction(DaoBankTransaction dao) {
        super();
        this.dao = dao;
    }

    @Override
    public final int getId() {
        return dao.getId();
    }

    public void setAccepted() {
        dao.setAccepted();
    }

    public void setRefused() {
        dao.setRefused();
    }

    public boolean setValidated() {
        return dao.setValidated();
    }

    public String getMessage() {
        return dao.getMessage();
    }

    public BigDecimal getValue() {
        return dao.getValue();
    }

    public State getState() {
        return dao.getState();
    }

    public Date getCreationDate() {
        return dao.getCreationDate();
    }

    public Date getModificationDate() {
        return dao.getModificationDate();
    }

    public String getReference() {
        return dao.getReference();
    }

    public String getToken() {
        return dao.getToken();
    }

    public void setProcessInformations(String processInformations) {
        dao.setProcessInformations(processInformations);
    }

    public String getProcessInformations() {
        return dao.getProcessInformations();
    }

}
