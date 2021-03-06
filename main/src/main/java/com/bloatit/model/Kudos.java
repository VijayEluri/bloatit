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

import com.bloatit.data.DaoKudos;
import com.bloatit.model.visitor.ModelClassVisitor;

public final class Kudos extends UserContent<DaoKudos> {

    // /////////////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTION
    // /////////////////////////////////////////////////////////////////////////////////////////

    private static final class MyCreator extends Creator<DaoKudos, Kudos> {
        @SuppressWarnings("synthetic-access")
        @Override
        public Kudos doCreate(final DaoKudos dao) {
            return new Kudos(dao);
        }
    }

    @SuppressWarnings("synthetic-access")
    public static Kudos create(final DaoKudos dao) {
        return new MyCreator().create(dao);
    }

    private Kudos(final DaoKudos dao) {
        super(dao);
    }

    public int getValue() {
        return getDao().getValue();
    }

    // /////////////////////////////////////////////////////////////////////////////////////////
    // Visitor
    // /////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public <ReturnType> ReturnType accept(final ModelClassVisitor<ReturnType> visitor) {
        return visitor.visit(this);
    }

}
