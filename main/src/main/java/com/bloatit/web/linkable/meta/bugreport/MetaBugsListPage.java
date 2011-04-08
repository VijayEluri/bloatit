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
package com.bloatit.web.linkable.meta.bugreport;

import static com.bloatit.framework.webserver.Context.tr;

import java.util.List;

import com.bloatit.framework.exceptions.lowlevel.RedirectException;
import com.bloatit.framework.meta.MetaBug;
import com.bloatit.framework.meta.MetaBugManager;
import com.bloatit.framework.webserver.annotations.ParamContainer;
import com.bloatit.framework.webserver.components.HtmlDiv;
import com.bloatit.framework.webserver.components.HtmlTitleBlock;
import com.bloatit.framework.webserver.components.renderer.HtmlMarkdownRenderer;
import com.bloatit.web.pages.IndexPage;
import com.bloatit.web.pages.master.Breadcrumb;
import com.bloatit.web.pages.master.MasterPage;
import com.bloatit.web.pages.master.TwoColumnLayout;
import com.bloatit.web.url.MembersListPageUrl;
import com.bloatit.web.url.MetaBugDeleteActionUrl;
import com.bloatit.web.url.MetaBugEditPageUrl;
import com.bloatit.web.url.MetaBugsListPageUrl;

@ParamContainer("meta/bug/list")
public final class MetaBugsListPage extends MasterPage {
    private final MetaBugsListPageUrl url;

    public MetaBugsListPage(final MetaBugsListPageUrl url) {
        super(url);
        this.url = url;
    }

    @Override
    protected void doCreate() throws RedirectException {
        final TwoColumnLayout layout = new TwoColumnLayout(true);
        List<MetaBug> bugList = MetaBugManager.getOpenBugs();

        final HtmlTitleBlock pageTitle = new HtmlTitleBlock(tr("Bugs list ({0})", bugList.size()), 1);

        for (MetaBug bug : bugList) {
            HtmlDiv bugBox = new HtmlDiv("meta_bug_box");
            HtmlDiv editBox = new HtmlDiv("float_right");
            bugBox.add(editBox);
            bugBox.add(new HtmlMarkdownRenderer(bug.getDescription()));
            editBox.add(new MetaBugEditPageUrl(bug.getId()).getHtmlLink(tr("edit")));
            editBox.addText(" - ");
            editBox.add(new MetaBugDeleteActionUrl(bug.getId()).getHtmlLink(tr("delete")));
            pageTitle.add(bugBox);
        }

        layout.addLeft(pageTitle);
        layout.addRight(new SideBarBugReportBlock(url));
        add(layout);
    }

    @Override
    protected String getPageTitle() {
        return "Bugs list";
    }

    @Override
    public boolean isStable() {
        return true;
    }

    @Override
    protected Breadcrumb getBreadcrumb() {
        return MetaBugsListPage.generateBreadcrumb();
    }

    public static Breadcrumb generateBreadcrumb() {
        final Breadcrumb breadcrumb = IndexPage.generateBreadcrumb();
        breadcrumb.pushLink(new MembersListPageUrl().getHtmlLink(tr("Members")));
        return breadcrumb;
    }
}