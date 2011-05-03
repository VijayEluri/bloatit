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
package com.bloatit.model.right;

import com.bloatit.model.Offer;

/**
 * The Class OfferRight store the properties accessor for the {@link Offer}
 * class.
 */
public class RgtJoinTeamInvitation extends RightManager {

    /**
     * The Class <code>Team</code> is a {@link RightManager.Public}
     * accessor for the <code>Team</code> property.
     */
    public static class Team extends Public {
        // TODO Switch this to a kind of weird private with 2 owners
        // nothing this is just a rename.
    }

    /**
     * The Class <code>Sender</code> is a {@link RightManager.Public}
     * accessor for the <code>Sender</code> property.
     */
    public static class Sender extends Public {
        // TODO Switch this to a kind of weird private with 2 owners
        // nothing this is just a rename.
    }

    /**
     * The Class <code>Reciever</code> is a {@link RightManager.Public}
     * accessor for the <code>Reciever</code> property.
     */
    public static class Receiver extends Public {
        // TODO Switch this to a kind of weird private with 2 owners
        // nothing this is just a rename.
    }
}
