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
package com.bloatit.model;

import java.math.BigDecimal;
import java.util.Date;

import com.bloatit.data.DaoExternalAccount;
import com.bloatit.data.DaoInternalAccount;
import com.bloatit.data.DaoTransaction;
import com.bloatit.data.exceptions.NotEnoughMoneyException;
import com.bloatit.framework.exceptions.highlevel.BadProgrammerException;
import com.bloatit.model.right.Action;
import com.bloatit.model.right.RgtAccount;
import com.bloatit.model.right.UnauthorizedOperationException;
import com.bloatit.model.visitor.ModelClassVisitor;

public final class Transaction extends Identifiable<DaoTransaction> {

    // /////////////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTION
    // /////////////////////////////////////////////////////////////////////////////////////////

    private static final class MyCreator extends Creator<DaoTransaction, Transaction> {
        @SuppressWarnings("synthetic-access")
        @Override
        public Transaction doCreate(final DaoTransaction dao) {
            return new Transaction(dao);
        }
    }

    @SuppressWarnings("synthetic-access")
    public static Transaction create(final DaoTransaction dao) {
        return new MyCreator().create(dao);
    }

    private Transaction(final DaoTransaction dao) {
        super(dao);
    }

    Transaction(final InternalAccount from, final Account<?> to, final BigDecimal amount) throws NotEnoughMoneyException {
        super(DaoTransaction.createAndPersist(from.getDao(), to.getDao(), amount));
    }

    // /////////////////////////////////////////////////////////////////////////////////////////
    // Accessor
    // /////////////////////////////////////////////////////////////////////////////////////////

    public boolean canAccessSomething() {
        return canAccess(new RgtAccount.Transaction(), Action.READ);
    }

    public InternalAccount getFrom() throws UnauthorizedOperationException {
        tryAccess(new RgtAccount.Transaction(), Action.READ);
        return getFromUnprotected();
    }

    protected InternalAccount getFromUnprotected() {
        return InternalAccount.create(getDao().getFrom());
    }

    protected Account<?> getToUnprotected() {
        if (getDao().getTo().getClass() == DaoInternalAccount.class) {
            return InternalAccount.create((DaoInternalAccount) getDao().getTo());
        } else if (getDao().getTo().getClass() == DaoExternalAccount.class) {
            return ExternalAccount.create((DaoExternalAccount) getDao().getTo());
        }
        throw new BadProgrammerException("Cannot find the right Account child class.");
    }

    public Account<?> getTo() throws UnauthorizedOperationException {
        tryAccess(new RgtAccount.Transaction(), Action.READ);
        return getToUnprotected();
    }

    public BigDecimal getAmount() throws UnauthorizedOperationException {
        tryAccess(new RgtAccount.Transaction(), Action.READ);
        return getDao().getAmount();
    }

    public Date getCreationDate() throws UnauthorizedOperationException {
        tryAccess(new RgtAccount.Transaction(), Action.READ);
        return getDao().getCreationDate();
    }

    // /////////////////////////////////////////////////////////////////////////////////////////
    // Visitor
    // /////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public <ReturnType> ReturnType accept(final ModelClassVisitor<ReturnType> visitor) {
        return visitor.visit(this);
    }
}
