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
package com.bloatit.web.linkable.members;

import static com.bloatit.framework.webprocessor.context.Context.tr;

import com.bloatit.framework.exceptions.highlevel.ShallNotPassException;
import com.bloatit.framework.exceptions.lowlevel.RedirectException;
import com.bloatit.framework.exceptions.lowlevel.UnauthorizedOperationException;
import com.bloatit.framework.utils.PageIterable;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.components.HtmlDiv;
import com.bloatit.framework.webprocessor.components.HtmlLink;
import com.bloatit.framework.webprocessor.components.HtmlRenderer;
import com.bloatit.framework.webprocessor.components.HtmlSpan;
import com.bloatit.framework.webprocessor.components.HtmlTitleBlock;
import com.bloatit.framework.webprocessor.components.advanced.HtmlClearer;
import com.bloatit.framework.webprocessor.components.meta.HtmlElement;
import com.bloatit.framework.webprocessor.components.meta.XmlNode;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.model.Member;
import com.bloatit.model.managers.MemberManager;
import com.bloatit.web.HtmlTools;
import com.bloatit.web.components.HtmlPagedList;
import com.bloatit.web.pages.IndexPage;
import com.bloatit.web.pages.master.Breadcrumb;
import com.bloatit.web.pages.master.MasterPage;
import com.bloatit.web.pages.master.sidebar.TwoColumnLayout;
import com.bloatit.web.url.MemberPageUrl;
import com.bloatit.web.url.MembersListPageUrl;

@ParamContainer("member/list")
public final class MembersListPage extends MasterPage {
    // Keep me here ! I am needed for the Url generation !
    private HtmlPagedList<Member> pagedMemberList;
    private final MembersListPageUrl url;

    public MembersListPage(final MembersListPageUrl url) {
        super(url);
        this.url = url;
    }

    @Override
    protected HtmlElement createBodyContent() throws RedirectException {
        final TwoColumnLayout layout = new TwoColumnLayout(true, url);

        final HtmlTitleBlock pageTitle = new HtmlTitleBlock("Members list", 1);
        final PageIterable<Member> memberList = MemberManager.getAll();
        final HtmlRenderer<Member> memberItemRenderer = new MemberRenderer();

        // TODO: avoid conflict
        final MembersListPageUrl clonedUrl = url.clone();
        pagedMemberList = new HtmlPagedList<Member>(memberItemRenderer, memberList, clonedUrl, clonedUrl.getPagedMemberListUrl());

        pageTitle.add(pagedMemberList);
        pageTitle.add(new HtmlClearer());

        layout.addLeft(pageTitle);

        return layout;
    }

    @Override
    protected String createPageTitle() {
        return "Members list";
    }

    @Override
    public boolean isStable() {
        return true;
    }

    private final class MemberRenderer implements HtmlRenderer<Member> {
        public MemberRenderer() {
            super();
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public XmlNode generate(final Member member) {
            final MemberPageUrl memberUrl = new MemberPageUrl(member);
            try {
                final HtmlDiv box = new HtmlDiv("member_box");

                box.add(new HtmlDiv("float_right").add(MembersTools.getMemberAvatar(member)));

                final HtmlDiv textBox = new HtmlDiv("member_text");
                HtmlLink htmlLink;
                htmlLink = memberUrl.getHtmlLink(member.getDisplayName());
                final HtmlSpan karma = new HtmlSpan("karma");
                karma.addText(HtmlTools.compressKarma(member.getKarma()));

                textBox.add(htmlLink);
                textBox.add(karma);
                box.add(textBox);
                box.add(new HtmlClearer());

                return box;
            } catch (final UnauthorizedOperationException e) {
                session.notifyError(Context.tr("An error prevented us from displaying user information. Please notify us."));
                throw new ShallNotPassException("User cannot access user information", e);
            }
        }
    }

    @Override
    protected Breadcrumb createBreadcrumb() {
        return MembersListPage.generateBreadcrumb();
    }

    protected static Breadcrumb generateBreadcrumb() {
        final Breadcrumb breadcrumb = IndexPage.generateBreadcrumb();

        breadcrumb.pushLink(new MembersListPageUrl().getHtmlLink(tr("Members")));

        return breadcrumb;
    }
}
