package com.bloatit.rest.resources;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import com.bloatit.data.DaoGroup.Right;
import com.bloatit.framework.exceptions.UnauthorizedOperationException;
import com.bloatit.framework.rest.RestElement;
import com.bloatit.framework.rest.RestServer.RequestMethod;
import com.bloatit.framework.rest.annotations.REST;
import com.bloatit.framework.rest.exception.RestException;
import com.bloatit.framework.webserver.masters.HttpResponse.StatusCode;
import com.bloatit.model.Group;
import com.bloatit.rest.list.RestGroupList;
import com.bloatit.rest.list.RestMemberList;

/**
 * <p>
 * Representation of a Group for the ReST RPC calls
 * </p>
 * <p>
 * This class should implement any methods from Group that needs to be called
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
 * return a RestGroupList</li>
 * </p>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class RestGroup extends RestElement<Group> {
    private Group model;

    // ---------------------------------------------------------------------------------------
    // -- Constructors
    // ---------------------------------------------------------------------------------------

    /**
     * Provided for JAXB
     */
    @SuppressWarnings("unused")
    private RestGroup() {
    }

    protected RestGroup(Group model) {
        this.model = model;
    }

    // ---------------------------------------------------------------------------------------
    // -- Static methods
    // ---------------------------------------------------------------------------------------

    /**
     * <p>
     * Finds the RestGroup matching the <code>id</code>
     * </p>
     *
     * @param id the id of the RestGroup
     */
    @REST(name = "groups", method = RequestMethod.GET)
    public static RestGroup getById(int id) {
        // TODO auto generated code
        // RestGroup restGroup = new RestGroup(GroupManager.getGroupById(id));
        // if (restGroup.isNull()) {
        // return null;
        // }
        // return restGroup;
        return null;
    }

    /**
     * <p>
     * Finds the list of all (valid) RestGroup
     * </p>
     */
    @REST(name = "groups", method = RequestMethod.GET)
    public static RestGroupList getAll() {
        // TODO auto generated code
        return null;
    }

    // ---------------------------------------------------------------------------------------
    // -- XML Getters
    // ---------------------------------------------------------------------------------------

    // TODO Generate

    @XmlAttribute
    @XmlID
    public String getId() {
        return model.getId().toString();
    }

    /**
     * @see com.bloatit.model.Group#getDescription()
     */
    // @XmlElement
    public String getDescription() {
        // TODO auto-generated code stub
        String description = model.getDescription();
        return description;
    }

    /**
     * @see com.bloatit.model.Group#getRight()
     */
    // @XmlElement
    public Right getRight() {
        // TODO auto-generated code stub
        Right right = model.getRight();
        return right;
    }

    /**
     * @see com.bloatit.model.Group#getMembers()
     */
    // @XmlElement
    public RestMemberList getMembers() {
        // TODO auto-generated code stub
        return new RestMemberList(model.getMembers());
    }

    /**
     * @see com.bloatit.model.Actor#getLogin()
     */
    // @XmlElement
    public String getLogin() throws RestException {
        // TODO auto-generated code stub
        try {
            String login = model.getLogin();
            return login;
        } catch (UnauthorizedOperationException e) {
            throw new RestException(StatusCode.ERROR_405_METHOD_NOT_ALLOWED, "Not allowed to use getLogin on Group", e);
        }
    }

    /**
     * @see com.bloatit.model.Actor#getInternalAccount()
     */
    // @XmlElement
    public RestInternalAccount getInternalAccount() throws RestException {
        // TODO auto-generated code stub
        try {
            RestInternalAccount internalAccount = new RestInternalAccount(model.getInternalAccount());
            return internalAccount;
        } catch (UnauthorizedOperationException e) {
            throw new RestException(StatusCode.ERROR_405_METHOD_NOT_ALLOWED, "Not allowed to use getInternalAccount on Group", e);
        }
    }

    /**
     * @see com.bloatit.model.Actor#getExternalAccount()
     */
    // @XmlElement
    public RestExternalAccount getExternalAccount() throws RestException {
        // TODO auto-generated code stub
        try {
            RestExternalAccount externalAccount = new RestExternalAccount(model.getExternalAccount());
            return externalAccount;
        } catch (UnauthorizedOperationException e) {
            throw new RestException(StatusCode.ERROR_405_METHOD_NOT_ALLOWED, "Not allowed to use getExternalAccount on Group", e);
        }
    }

    /**
     * @see com.bloatit.model.Actor#getEmail()
     */
    // @XmlElement
    public String getEmail() throws RestException {
        // TODO auto-generated code stub
        try {
            String email = model.getEmail();
            return email;
        } catch (UnauthorizedOperationException e) {
            throw new RestException(StatusCode.ERROR_405_METHOD_NOT_ALLOWED, "Not allowed to use getEmail on Group", e);
        }
    }

    /**
     * @see com.bloatit.model.Actor#getDateCreation()
     */
    // @XmlElement
    public Date getDateCreation() throws RestException {
        // TODO auto-generated code stub
        try {
            Date dateCreation = model.getDateCreation();
            return dateCreation;
        } catch (UnauthorizedOperationException e) {
            throw new RestException(StatusCode.ERROR_405_METHOD_NOT_ALLOWED, "Not allowed to use getDateCreation on Group", e);
        }
    }

    // XXX Do something
    // /**
    // * @see com.bloatit.model.Actor#getBankTransactions()
    // */
    // // @XmlElement
    // public RestTransactionList getBankTransactions() throws RestException {
    // // TODO auto-generated code stub
    // try {
    // // RestList<?> bankTransactions = new
    // // RestList<?>(model.getBankTransactions());
    // return new RestTransactionList(model.getBankTransactions());
    // } catch (UnauthorizedOperationException e) {
    // throw new RestException(StatusCode.ERROR_405_METHOD_NOT_ALLOWED,
    // "Not allowed to use getBankTransactions on Group", e);
    // }
    // }

    // ---------------------------------------------------------------------------------------
    // -- Utils
    // ---------------------------------------------------------------------------------------

    /**
     * Provided for JAXB
     */
    void setModel(Group model) {
        this.model = model;
    }

    /**
     * Package method to find the model
     */
    Group getModel() {
        return model;
    }

    @Override
    public boolean isNull() {
        return (model == null);
    }

}
