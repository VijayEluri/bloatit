package com.bloatit.web.html.pages.master;

import com.bloatit.common.UnauthorizedOperationException;
import com.bloatit.framework.InternalAccount;
import com.bloatit.framework.Member;
import com.bloatit.web.html.HtmlBranch;
import com.bloatit.web.html.HtmlText;
import com.bloatit.web.html.HtmlTools;
import com.bloatit.web.html.components.standard.HtmlDiv;
import com.bloatit.web.html.components.standard.HtmlLink;
import com.bloatit.web.html.components.standard.HtmlSpan;
import com.bloatit.web.server.Context;
import com.bloatit.web.server.Session;
import com.bloatit.web.utils.i18n.CurrencyLocale;
import com.bloatit.web.utils.url.AccountChargingPageUrl;
import com.bloatit.web.utils.url.LoginPageUrl;
import com.bloatit.web.utils.url.LogoutActionUrl;
import com.bloatit.web.utils.url.MyAccountPageUrl;

public class TopBar extends HtmlDiv {

    protected TopBar() {
        super();

        setId("top_bar");

        final Session session = Context.getSession();
        if (session.isLogged()) {
            // Display user name
            String displayName = "John Doe";
            try {
                displayName = session.getAuthToken().getMember().getDisplayName();
            } catch (UnauthorizedOperationException e) {
                // no right, leave invalid name
            }
            final HtmlLink memberLink = new MyAccountPageUrl().getHtmlLink(displayName);

            // Display user karma
            final HtmlBranch karma = new HtmlSpan();
            karma.setCssClass("karma");
            try {
                karma.addText(HtmlTools.compressKarma(session.getAuthToken().getMember().getKarma()));
            } catch (UnauthorizedOperationException e) {
                // No right, no display the karma
            }
            add(new HtmlSpan().setCssClass("top_bar_component").add(memberLink).add(karma));

            // Display user money in euro
            final HtmlBranch euroMoney = new HtmlSpan();
            euroMoney.setCssClass("euro_money");

            final Member member = session.getAuthToken().getMember();
            member.authenticate(session.getAuthToken());
            InternalAccount internalAccount;
            try {
                internalAccount = member.getInternalAccount();
                internalAccount.authenticate(session.getAuthToken());
                CurrencyLocale cl = Context.getLocalizator().getCurrency(internalAccount.getAmount());
                euroMoney.add(new HtmlText(cl.getDefaultString()));

                final HtmlBranch money = new AccountChargingPageUrl().getHtmlLink(euroMoney);
                money.setCssClass("money");

                // Display user money in locale money (when needed)
                if (cl.availableTargetCurrency() && !cl.getDefaultString().equals(cl.getLocaleString())) {
                    final HtmlBranch localeMoney = new HtmlSpan();
                    localeMoney.setCssClass("locale_money");

                    localeMoney.addText(cl.getLocaleString());
                    money.add(localeMoney);
                }
                add(new HtmlSpan().setCssClass("top_bar_component").add(money));
            } catch (UnauthorizedOperationException e) {
                // no right, no money displayed
            }


            // Display logout link
            final HtmlLink logoutLink = new LogoutActionUrl().getHtmlLink(Context.tr("Logout"));
            add(new HtmlSpan().setCssClass("top_bar_component").add(logoutLink));

        } else {
            final HtmlLink loginLink = new LoginPageUrl().getHtmlLink(Context.tr("Login / Signup"));
            add(new HtmlSpan().setCssClass("top_bar_component").add(loginLink));
        }
    }
}
