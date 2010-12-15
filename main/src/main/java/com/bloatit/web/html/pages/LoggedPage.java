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

package com.bloatit.web.html.pages;

import com.bloatit.web.exceptions.RedirectException;
import com.bloatit.web.html.HtmlElement;
import com.bloatit.web.html.pages.master.Page;
import com.bloatit.web.utils.url.Url;
import com.bloatit.web.utils.url.UrlBuilder;

public abstract class LoggedPage extends Page {
    private final Url loggedUrl;

    protected LoggedPage(final Url loggedUrl) throws RedirectException {
        super();
        this.loggedUrl = loggedUrl;
    }

    @Override
    public void create() throws RedirectException {
        super.create();

        if (session.isLogged()) {
            add(generateRestrictedContent());
        } else {
            session.notifyBad(getRefusalReason());
            session.setTargetPage(loggedUrl.toString());
            throw new RedirectException(new UrlBuilder(LoginPage.class).buildUrl());
        }
    }

    public abstract HtmlElement generateRestrictedContent();

    public abstract String getRefusalReason();

}
