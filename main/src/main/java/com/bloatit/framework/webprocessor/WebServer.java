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
package com.bloatit.framework.webprocessor;

import java.io.IOException;

import com.bloatit.common.Log;
import com.bloatit.framework.exceptions.highlevel.ExternalErrorException;
import com.bloatit.framework.exceptions.highlevel.ShallNotPassException;
import com.bloatit.framework.exceptions.lowlevel.RedirectException;
import com.bloatit.framework.utils.parameters.Parameters;
import com.bloatit.framework.webprocessor.masters.HttpResponse;
import com.bloatit.framework.webprocessor.masters.Linkable;
import com.bloatit.framework.webprocessor.url.PageNotFoundUrl;
import com.bloatit.framework.xcgiserver.HttpHeader;
import com.bloatit.framework.xcgiserver.HttpPost;
import com.bloatit.framework.xcgiserver.XcgiProcessor;

public abstract class WebServer implements XcgiProcessor {

    public WebServer() {
        super();
    }

    @Override
    public final boolean process(final HttpHeader httpHeader, final HttpPost post, final HttpResponse response) throws IOException {
        final Session session = findSession(httpHeader);

        try {
            final WebHeader header = new WebHeader(httpHeader);
            
            ModelAccessor.open();
            Context.reInitializeContext(header, session);

            final String pageCode = header.getPageName();

            // Merge post and get parameters.
            final Parameters parameters = new Parameters();
            parameters.putAll(header.getParameters());
            parameters.putAll(header.getGetParameters());
            parameters.putAll(post.getParameters());

            try {
                final Linkable linkable = constructLinkable(pageCode, parameters, session);
                linkable.writeToHttp(response, this);
            } catch (final ShallNotPassException e) {
                Log.framework().fatal("Right management error", e);
                // TODO create a page dedicated to handling this
                Context.getSession().notifyError("TODO : This page is a placeholder used to handle right management errors.");
                final Linkable linkable = constructLinkable(PageNotFoundUrl.getName(), parameters, session);
                try {
                    linkable.writeToHttp(response, this);
                } catch (final RedirectException e1) {
                    throw new ExternalErrorException("Cannot create error page after and error in right management.", e1);
                }
            } catch (final PageNotFoundException e) {
                Log.framework().info("Page not found", e);
                final Linkable linkable = constructLinkable(PageNotFoundUrl.getName(), parameters, session);
                try {
                    linkable.writeToHttp(response, this);
                } catch (final RedirectException e1) {
                    Log.framework().info("Redirect to " + e.getUrl(), e);
                    response.writeRedirect(e.getUrl().urlString());
                }
            } catch (final RedirectException e) {
                Log.framework().info("Redirect to " + e.getUrl(), e);
                response.writeRedirect(e.getUrl().urlString());
            }
            ModelAccessor.close();

        } catch (final RuntimeException e) {
            response.writeException(e);
            try {
                ModelAccessor.rollback();
            } catch (final RuntimeException e1) {
                Log.framework().fatal(e);
                throw e1;
            }
            throw e;
        }
        return true;
    }

    public abstract Linkable constructLinkable(final String pageCode, final Parameters params, final Session session);

    /**
     * Return the session for the user. Either an existing session or a new
     * session.
     * 
     * @param header
     * @return the session matching the user
     */
    private Session findSession(final HttpHeader header) {
        final String key = header.getHttpCookie().get("session_key");
        Session sessionByKey = null;
        if (key != null && (sessionByKey = SessionManager.getByKey(key)) != null) {
            if (sessionByKey.isExpired()) {
                SessionManager.destroySession(sessionByKey);
                // A new session will be create
            } else {
                sessionByKey.resetExpirationTime();
                return sessionByKey;
            }
        }
        return SessionManager.createSession();
    }

}