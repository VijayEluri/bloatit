/*
 * Copyright (C) 2010 BloatIt. This file is part of BloatIt. BloatIt is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * BloatIt is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details. You should have received a copy of the GNU Affero General Public
 * License along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */

package com.bloatit.framework.webprocessor.context;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.codec.digest.DigestUtils;

import com.bloatit.framework.FrameworkConfiguration;
import com.bloatit.framework.utils.Hash;
import com.bloatit.framework.utils.datetime.DateUtils;
import com.bloatit.framework.utils.parameters.SessionParameters;
import com.bloatit.framework.webprocessor.ErrorMessage;
import com.bloatit.framework.webprocessor.ErrorMessage.Level;
import com.bloatit.framework.webprocessor.components.meta.HtmlNode;
import com.bloatit.framework.webprocessor.components.meta.HtmlText;
import com.bloatit.framework.webprocessor.url.Url;
import com.bloatit.framework.webprocessor.url.UrlDump;
import com.bloatit.framework.webprocessor.url.UrlParameter;
import com.bloatit.web.linkable.master.WebProcess;
import com.bloatit.web.url.IndexPageUrl;

/**
 * A class to handle the user session on the web server
 * <p>
 * A session starts when the user arrives on the server (first GET request).
 * When the user login, his sessions continues (he'll therefore keep all his
 * session informations), but he simply gets a new authtoken that says he's
 * logged
 * </p>
 * <p>
 * Session is used for various purposes :
 * <li>Store some parameters {@link Session#addParameter(UrlParameter)}</li>
 * <li>Store pages that the user wishes to consult, but he couldn't because he
 * didn't meet the requirements</li>
 * </p>
 */
public final class Session {
    private static final int SHA1_SIZE = 20;
    public static final String SECURE_TOKEN_NAME = "secure";

    private long expirationTime;
    private final String shortKey;
    private String key;
    private final String ipAddress;

    private Integer memberId;
    private Locale memberLocale;

    private final Deque<ErrorMessage> notificationList = new LinkedBlockingDeque<ErrorMessage>();
    private final SessionParameters parameters = new SessionParameters();

    private UrlDump lastStablePage = null;
    private UrlDump targetPage = null;
    private UrlDump lastVisitedPage;

    private final Map<String, WebProcess> processes = Collections.synchronizedMap(new HashMap<String, WebProcess>());

    /**
     * Construct a session based on the information from <code>id</code>
     * 
     * @param id the id of the session
     */
    protected Session(final String key, final String ipAddress) {
        this.key = key;
        this.shortKey = Hash.shortHash(key);
        this.ipAddress = ipAddress;
        this.memberId = null;

        resetExpirationTime();
    }

    /**
     * Restore session
     * 
     * @param key
     * @param ipAddress
     * @param country
     * @param language
     */
    protected Session(final String key, final String shortKey, final String ipAddress, final Integer memberId, String language, String country) {
        this.key = key;
        this.shortKey = shortKey;
        this.ipAddress = ipAddress;
        this.memberId = memberId;
        this.memberLocale = new Locale(language, country);

        resetExpirationTime();
    }

    public synchronized String getKey() {
        return key;
    }

    public synchronized String getShortKey() {
        return shortKey;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public synchronized void logIn(final Integer memberId, final Locale memberLocale) {
        this.memberLocale = memberLocale;
        this.memberId = memberId;
        resetExpirationTime();

        // re create the session key.
        key = SessionManager.generateNewSessionKey(key);

        // The targets pages in the session can have the old secure keys.
        // So we have to replace them with the new ones (no problem we just
        // log in).
        if (targetPage != null) {
            targetPage.addOrReplaceParameter(SECURE_TOKEN_NAME, getShortKey());
        }
    }

    public synchronized void logOut() {
        this.memberId = null;
        resetExpirationTime();

        // re create the session key.
        key = SessionManager.generateNewSessionKey(key);
    }

    public synchronized Integer getMemberId() {
        return memberId;
    }

    public synchronized Locale getMemberLocale() {
        return memberLocale;
    }

    public synchronized void resetExpirationTime() {
        if (memberId != null) {
            expirationTime = Context.getResquestTime() + FrameworkConfiguration.getSessionLoggedDuration() * DateUtils.SECOND_PER_DAY;
        } else {
            expirationTime = Context.getResquestTime() + FrameworkConfiguration.getSessionDefaultDuration() * DateUtils.SECOND_PER_DAY;
        }
    }

    public synchronized boolean isExpired() {
        return DateUtils.now().getTime() / DateUtils.MILLISECOND_PER_SECOND > expirationTime;
    }

    public synchronized void setLastStablePage(final Url p) {
        if (p == null) {
            this.lastStablePage = null;
        } else {
            this.lastStablePage = new UrlDump(p);
        }
    }

    /**
     * Finds the <i>supposedly</i> best page to redirect the user to
     * <p>
     * Actions are ignored. <br />
     * <b>NOTE</b>: If the user has two active tabs, it will return the last
     * page visited in any tab, and can lead to <i>very</i> confusing result.
     * Avoid relying on this.
     * </p>
     * 
     * @return the best page to redirect the user to
     */
    public synchronized Url pickPreferredPage() {
        if (targetPage != null) {
            final Url tempStr = targetPage;
            targetPage = null;
            return tempStr;
        } else if (lastStablePage != null) {
            return lastStablePage;
        }
        return new IndexPageUrl();

    }

    /**
     * Finds the last page the user visited on the website.
     * <p>
     * Actions are ignored. <br />
     * <b>NOTE</b>: If the user has two active tabs, it will return the last
     * page visited in any tab, and can lead to <i>very</i> confusing result.
     * Avoid relying on this.
     * </p>
     * 
     * @return the last page the user visited
     */
    public synchronized Url getLastVisitedPage() {
        if (lastVisitedPage != null) {
            return lastVisitedPage;
        }
        return new IndexPageUrl();
    }

    /**
     * Finds the last stable page the user visited on the website.
     * <p>
     * Actions are ignored. <br />
     * <b>NOTE</b>: If the user has two active tabs, it will return the last
     * page visited in any tab, and can lead to <i>very</i> confusing result.
     * Avoid relying on this.
     * </p>
     * 
     * @return the last page the user visited
     */
    public synchronized Url getLastStablePage() {
        if (lastStablePage != null) {
            return lastStablePage;
        }
        return new IndexPageUrl();
    }

    public synchronized void setLastVisitedPage(final Url lastVisitedPage) {
        if (lastVisitedPage == null) {
            this.lastVisitedPage = null;
        } else {
            this.lastVisitedPage = new UrlDump(lastVisitedPage);
        }
    }

    public final synchronized void setTargetPage(final Url targetPage) {
        if (targetPage == null) {
            this.targetPage = null;
        } else {
            this.targetPage = new UrlDump(targetPage);
        }
    }

    public final synchronized void notifyGood(final String message) {
        notificationList.add(new ErrorMessage(Level.INFO, new HtmlText(message)));
    }

    public final synchronized void notifyWarning(final String message) {
        notificationList.add(new ErrorMessage(Level.WARNING, new HtmlText(message)));
    }

    public final synchronized void notifyError(final String message) {
        notificationList.add(new ErrorMessage(Level.FATAL, new HtmlText(message)));
    }

    public final synchronized void notifyGood(final HtmlNode message) {
        notificationList.add(new ErrorMessage(Level.INFO, message));
    }

    public final synchronized void notifyWarning(final HtmlNode message) {
        notificationList.add(new ErrorMessage(Level.WARNING, message));
    }

    public final synchronized void notifyError(final HtmlNode message) {
        notificationList.add(new ErrorMessage(Level.FATAL, message));
    }

    public final synchronized void flushNotifications() {
        notificationList.clear();
    }

    public final synchronized Deque<ErrorMessage> getNotifications() {
        final String gn = Context.getGlobalNotification();
        if (gn != null) {
            notifyError(gn);
        }
        return notificationList;
    }

    /**
     * Finds all the session parameters
     * 
     * @return the parameter of the session
     * @deprecated use a RequestParam
     */
    @Deprecated
    public final synchronized SessionParameters getParameters() {
        return parameters;
    }

    @SuppressWarnings("unchecked")
    public final synchronized <T, U> UrlParameter<T, U> pickParameter(final UrlParameter<T, U> param) {
        return (UrlParameter<T, U>) parameters.pick(param.getName());
    }

    public final synchronized void addParameter(final UrlParameter<?, ?> param) {
        if (!(param.getValue() == null && param.getStringValue() == null)) {
            parameters.add(param.getName(), param);
        }
    }

    public final synchronized String createWebProcess(final WebProcess process) {
        int length = 5;
        String key = null;
        while (key == null) {
            final String tempKey = DigestUtils.sha256Hex(UUID.randomUUID().toString()).substring(0, length);

            if (processes.containsKey(tempKey)) {
                if (length < SHA1_SIZE) {
                    length++;
                }
            } else {
                key = tempKey;
            }

        }
        processes.put(key, process);
        return key;
    }

    public final synchronized WebProcess getWebProcess(final String processId) {
        return processes.get(processId);
    }

    public final synchronized void destroyWebProcess(final WebProcess webProcess) {
        processes.remove(webProcess.getId());
    }

    public boolean isValid(final String ipAddress) {
        return !isExpired() && (this.ipAddress == ipAddress || (this.ipAddress != null && this.ipAddress.equals(ipAddress)));
    }
}
