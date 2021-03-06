/*
 * Copyright (C) 2010 BloatIt.
 *
 * This file is part of BloatIt.
 *
 * BloatIt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BloatIt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bloatit.rest.resources;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.bloatit.framework.restprocessor.RestElement;
import com.bloatit.framework.restprocessor.RestServer.RequestMethod;
import com.bloatit.framework.restprocessor.annotations.REST;
import com.bloatit.model.Team;
import com.bloatit.model.managers.TeamManager;
import com.bloatit.model.right.UnauthorizedOperationException;
import com.bloatit.rest.adapters.DateAdapter;
import com.bloatit.rest.list.RestMemberList;
import com.bloatit.rest.list.RestTeamList;

/**
 * <p>
 * Representation of a Team for the ReST RPC calls
 * </p>
 * <p>
 * This class should implement any methods from Team that needs to be called
 * through the ReST RPC. Every such method needs to be mapped with the
 * {@code @REST} interface.
 * <p>
 * ReST uses the four HTTP request methods <code>GET</code>, <code>POST</code>,
 * <code>PUT</code>, <code>DELETE</code> each with their own meaning. Please
 * only bind the according to the following:
 * <li>GET list: List the URIs and perhaps other details of the collection's
 * members.</li>
 * <li>GET list/id: Retrieve a representation of the addressed member of the
 * collection, expressed in an appropriate Internet media type.</li>
 * <li>POST list: Create a new entry in the collection. The new entry's URL is
 * assigned automatically and is usually returned by the operation.</li>
 * <li>POST list/id: Treat the addressed member as a collection in its own right
 * and create a new entry in it.</li>
 * <li>PUT list: Replace the entire collection with another collection.</li>
 * <li>PUT list/id: Replace the addressed member of the collection, or if it
 * doesn't exist, create it.</li>
 * <li>DELETE list: Delete the entire collection.</li>
 * <li>DELETE list/id: Delete the addressed member of the collection.</li>
 * </p>
 * </p>
 * <p>
 * This class will be serialized as XML (or maybe JSON who knows) to be sent
 * over to the client RPC. Hence this class needs to be annotated to indicate
 * which methods (and/or fields) are to be matched in the XML data. For this
 * use:
 * <li>@XmlRootElement at the root of the class</li>
 * <li>@XmlElement on each method/attribute that will yield <i>complex</i> data</li>
 * <li>@XmlAttribute on each method/attribute that will yield <i>simple</i> data
 * </li>
 * <li>Methods that return a list need to be annotated with @XmlElement and to
 * return a RestTeamList</li>
 * </p>
 */
@XmlRootElement(name = "team")
@XmlAccessorType(XmlAccessType.NONE)
public class RestTeam extends RestElement<Team> {
    private Team model;

    // ---------------------------------------------------------------------------------------
    // -- Constructors
    // ---------------------------------------------------------------------------------------

    /**
     * Provided for JAXB
     */
    @SuppressWarnings("unused")
    private RestTeam() {
        super();
    }

    protected RestTeam(final Team model) {
        this.model = model;
    }

    // ---------------------------------------------------------------------------------------
    // -- Static methods
    // ---------------------------------------------------------------------------------------

    /**
     * <p>
     * Finds the RestTeam matching the <code>id</code>
     * </p>
     * 
     * @param id the id of the RestTeam
     */
    @REST(name = "teams", method = RequestMethod.GET)
    public static RestTeam getById(final int id) {
        final RestTeam restTeam = new RestTeam(TeamManager.getById(id));
        if (restTeam.isNull()) {
            return null;
        }
        return restTeam;
    }

    /**
     * <p>
     * Finds the list of all (valid) RestTeam
     * </p>
     */
    @REST(name = "teams", method = RequestMethod.GET)
    public static RestTeamList getAll() {
        return new RestTeamList(TeamManager.getAll());
    }

    // ---------------------------------------------------------------------------------------
    // -- XML Getters
    // ---------------------------------------------------------------------------------------

    @XmlAttribute
    @XmlID
    public String getId() {
        return model.getId().toString();
    }

    /**
     * @see com.bloatit.model.Team#getDescription()
     */
    @XmlElement
    public String getDescription() {
        return model.getDescription();
    }

    /**
     * @see com.bloatit.model.Team#getMembers()
     */
    @XmlElement
    public RestMemberList getMembers() {
        return new RestMemberList(model.getMembers());
    }

    /**
     * @see com.bloatit.model.Actor#getLogin()
     */
    @XmlAttribute
    public String getName() {
        return model.getLogin();
    }

    /**
     * @see com.bloatit.model.Actor#getInternalAccount()
     */
    @XmlElement
    public RestInternalAccount getInternalAccount() {
        try {
            return new RestInternalAccount(model.getInternalAccount());
        } catch (final UnauthorizedOperationException e) {
            return null; // throw new
                         // RestException(StatusCode.ERROR_405_METHOD_NOT_ALLOWED,
            // "Not allowed to use getInternalAccount on Team", e);
        }
    }

    /**
     * @see com.bloatit.model.Actor#getExternalAccount()
     */
    @XmlElement
    public RestExternalAccount getExternalAccount() {
        try {
            return new RestExternalAccount(model.getExternalAccount());
        } catch (final UnauthorizedOperationException e) {
            return null; // throw new
                         // RestException(StatusCode.ERROR_405_METHOD_NOT_ALLOWED,
            // "Not allowed to use getExternalAccount on Team", e);
        }
    }

    @XmlAttribute
    public String getContacts() {
        return model.getPublicContact();
    }

    /**
     * @see com.bloatit.model.Actor#getDateCreation()
     */
    @XmlAttribute(name = "creationdate")
    @XmlJavaTypeAdapter(DateAdapter.class)
    public Date getDateCreation() {
        try {
            return model.getDateCreation();
        } catch (final UnauthorizedOperationException e) {
            return null; // throw new
                         // RestException(StatusCode.ERROR_405_METHOD_NOT_ALLOWED,
            // "Not allowed to use getDateCreation on Team", e);
        }
    }

    // ---------------------------------------------------------------------------------------
    // -- Utils
    // ---------------------------------------------------------------------------------------

    /**
     * Provided for JAXB
     */
    void setModel(final Team model) {
        this.model = model;
    }

    /**
     * Package method to find the model
     */
    Team getModel() {
        return model;
    }

    @Override
    public boolean isNull() {
        return (model == null);
    }

}
