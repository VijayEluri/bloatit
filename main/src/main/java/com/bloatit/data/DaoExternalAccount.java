//
// Copyright (c) 2011 Linkeos.
//
// This file is part of Elveos.org.
// Elveos.org is free software: you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation, either version 3 of the License, or (at your
// option) any later version.
//
// Elveos.org is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
// more details.
// You should have received a copy of the GNU General Public License along
// with Elveos.org. If not, see http://www.gnu.org/licenses/.
//
package com.bloatit.data;

import java.math.BigDecimal;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.bloatit.framework.exceptions.lowlevel.NonOptionalParameterException;

/**
 * An external Account is our vision of a "real" bank account.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class DaoExternalAccount extends DaoAccount {

    /**
     * Ok for now there is only IBAN code but they may have other types.
     */
    public enum AccountType {

        /** The IBAN. */
        IBAN,

        /** The VIRTUAL. */
        VIRTUAL
    }

    /**
     * This is for tests only there is no check here it is a simple string.
     */
    @Basic(optional = false)
    private String bankCode;
    @Basic(optional = false)
    @Enumerated
    private AccountType type;

    /**
     * Create and persist a DaoExternalAccount.
     * 
     * @param actor the person that possess the account
     * @param type the type of account
     * @param bankCode the code to give back some money on it
     * @return the newly created external account.
     * @see DaoExternalAccount#DaoExternalAccount(DaoActor,
     *      DaoExternalAccount.AccountType, String)
     */
    public static DaoExternalAccount createAndPersist(final DaoActor actor, final AccountType type, final String bankCode) {
        final Session session = SessionManager.getSessionFactory().getCurrentSession();
        final DaoExternalAccount account = new DaoExternalAccount(actor, type, bankCode);
        try {
            session.save(account);
        } catch (final HibernateException e) {
            session.getTransaction().rollback();
            throw e;
        }
        return account;
    }

    /**
     * Create a new External account.
     * 
     * @param actor is the owner of the account
     * @param type is the account type
     * @param bankCode is the bank code (for now IBAN...) THERE IS NO CHECK HERE
     * @throws NonOptionalParameterException if any of the parameter is null
     * @throws anExceptionToDefine when we will check the validity of the IBAN
     *             we will have to throw an exception if its not valid.
     */
    private DaoExternalAccount(final DaoActor actor, final AccountType type, final String bankCode) {
        super(actor);
        if (type == null || bankCode == null || bankCode.isEmpty()) {
            throw new NonOptionalParameterException();
        }
        this.type = type;
        this.bankCode = bankCode;
    }

    /**
     * Instantiates a new dao external account.
     * 
     * @param actor the actor
     */
    protected DaoExternalAccount(final DaoActor actor) {
        super(actor);
        this.type = AccountType.VIRTUAL;
        this.bankCode = "";
    }

    /**
     * Sets the this is for tests only there is no check here it is a simple
     * string.
     * 
     * @param bankCode the new this is for tests only there is no check here it
     *            is a simple string
     */
    public void setBankCode(final String bankCode) {
        this.bankCode = bankCode;
    }

    /**
     * Sets the type.
     * 
     * @param type the new type
     */
    public void setType(final AccountType type) {
        this.type = type;
    }

    /**
     * Gets the this is for tests only there is no check here it is a simple
     * string.
     * 
     * @return the this is for tests only there is no check here it is a simple
     *         string
     */
    public String getBankCode() {
        return this.bankCode;
    }

    /**
     * Gets the type.
     * 
     * @return the type
     */
    public AccountType getType() {
        return this.type;
    }

    /**
     * Return true all the time.
     * 
     * @param amount the amount
     * @return true, if successful
     */
    @Override
    protected boolean hasEnoughMoney(final BigDecimal amount) {
        return true;
    }

    // ======================================================================
    // Visitor.
    // ======================================================================

    /*
     * (non-Javadoc)
     * @see
     * com.bloatit.data.DaoIdentifiable#accept(com.bloatit.data.DataClassVisitor
     * )
     */
    @Override
    public <ReturnType> ReturnType accept(final DataClassVisitor<ReturnType> visitor) {
        return visitor.visit(this);
    }

    // ======================================================================
    // For hibernate mapping
    // ======================================================================

    /**
     * Instantiates a new dao external account.
     */
    protected DaoExternalAccount() {
        super();
    }

}
