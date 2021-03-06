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

import java.math.BigDecimal;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.bloatit.data.DaoMilestone.MilestoneState;
import com.bloatit.framework.restprocessor.RestElement;
import com.bloatit.framework.restprocessor.RestServer.RequestMethod;
import com.bloatit.framework.restprocessor.annotations.REST;
import com.bloatit.model.Milestone;
import com.bloatit.model.managers.MilestoneManager;
import com.bloatit.rest.adapters.DateAdapter;
import com.bloatit.rest.list.RestMilestoneList;
import com.bloatit.rest.list.RestReleaseList;

/**
 * <p>
 * Representation of a Milestone for the ReST RPC calls
 * </p>
 * <p>
 * This class should implement any methods from Milestone that needs to be
 * called through the ReST RPC. Every such method needs to be mapped with the
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
 * return a RestMilestoneList</li>
 * </p>
 */
@XmlRootElement(name = "milestone")
@XmlAccessorType(XmlAccessType.NONE)
public class RestMilestone extends RestElement<Milestone> {
    private Milestone model;

    // ---------------------------------------------------------------------------------------
    // -- Constructors
    // ---------------------------------------------------------------------------------------

    /**
     * Provided for JAXB
     */
    @SuppressWarnings("unused")
    private RestMilestone() {
        super();
    }

    protected RestMilestone(final Milestone model) {
        this.model = model;
    }

    // ---------------------------------------------------------------------------------------
    // -- Static methods
    // ---------------------------------------------------------------------------------------

    /**
     * <p>
     * Finds the RestMilestone matching the <code>id</code>
     * </p>
     * 
     * @param id the id of the RestMilestone
     */
    @REST(name = "milestones", method = RequestMethod.GET)
    public static RestMilestone getById(final int id) {
        final RestMilestone restMilestone = new RestMilestone(MilestoneManager.getById(id));
        if (restMilestone.isNull()) {
            return null;
        }
        return restMilestone;
    }

    /**
     * <p>
     * Finds the list of all (valid) RestMilestone
     * </p>
     */
    @REST(name = "milestones", method = RequestMethod.GET)
    public static RestMilestoneList getAll() {
        return new RestMilestoneList(MilestoneManager.getAll());
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
     * @see com.bloatit.model.Milestone#getOffer()
     */
    @XmlElement
    @XmlIDREF
    public RestOffer getOffer() {
        final RestOffer offer = new RestOffer(model.getOffer());
        if (offer.isNull()) {
            return null;
        }
        return offer;
    }

    /**
     * @see com.bloatit.model.Milestone#getReleaseDate()
     */
    @XmlAttribute
    @XmlJavaTypeAdapter(DateAdapter.class)
    public Date getReleaseDate() {
        return model.getReleaseDate();
    }

    /**
     * @see com.bloatit.model.Milestone#getFatalBugsPercent()
     */
    @XmlElement
    public int getFatalBugsPercent() {
        return model.getFatalBugsPercent();
    }

    /**
     * @see com.bloatit.model.Milestone#getMajorBugsPercent()
     */
    @XmlElement
    public int getMajorBugsPercent() {
        return model.getMajorBugsPercent();
    }

    /**
     * @see com.bloatit.model.Milestone#getMinorBugsPercent()
     */
    @XmlElement
    public int getMinorBugsPercent() {
        return model.getMinorBugsPercent();
    }

    /**
     * @see com.bloatit.model.Milestone#getExpirationDate()
     */
    @XmlAttribute
    @XmlJavaTypeAdapter(DateAdapter.class)
    public Date getExpirationDate() {
        return model.getExpirationDate();
    }

    /**
     * @see com.bloatit.model.Milestone#getAmount()
     */
    @XmlAttribute
    public BigDecimal getAmount() {
        return model.getAmount();
    }

    /**
     * @see com.bloatit.model.Milestone#getTitle()
     */
    @XmlElement
    public String getTitle() {
        return model.getTitle();
    }

    /**
     * @see com.bloatit.model.Milestone#getDescription()
     */
    @XmlElement
    public String getDescription() {
        return model.getDescription();
    }

    /**
     * @see com.bloatit.model.Milestone#getPosition()
     */
    @XmlElement
    public int getPosition() {
        return model.getPosition();
    }

    /**
     * @see com.bloatit.model.Milestone#getMilestoneState()
     */
    @XmlElement
    public MilestoneState getMilestoneState() {
        return model.getMilestoneState();
    }

    /**
     * @see com.bloatit.model.Milestone#getReleases()
     */
    @XmlElement
    public RestReleaseList getReleases() {
        return new RestReleaseList(model.getReleases());
    }

    // ---------------------------------------------------------------------------------------
    // -- Utils
    // ---------------------------------------------------------------------------------------

    /**
     * Provided for JAXB
     */
    void setModel(final Milestone model) {
        this.model = model;
    }

    /**
     * Package method to find the model
     */
    Milestone getModel() {
        return model;
    }

    @Override
    public boolean isNull() {
        return (model == null);
    }

}
