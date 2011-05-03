package com.bloatit.web.linkable.admin.withdraw;

import com.bloatit.data.DaoMoneyWithdrawal.State;
import com.bloatit.framework.exceptions.lowlevel.UnauthorizedOperationException;
import com.bloatit.framework.webprocessor.annotations.Optional;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.components.HtmlDiv;
import com.bloatit.framework.webprocessor.components.HtmlLink;
import com.bloatit.framework.webprocessor.components.HtmlTitle;
import com.bloatit.framework.webprocessor.components.advanced.HtmlSimpleLineTable;
import com.bloatit.framework.webprocessor.components.form.FieldData;
import com.bloatit.framework.webprocessor.components.form.HtmlCheckbox;
import com.bloatit.framework.webprocessor.components.form.HtmlDropDown;
import com.bloatit.framework.webprocessor.components.form.HtmlForm;
import com.bloatit.framework.webprocessor.components.form.HtmlFormField.LabelPosition;
import com.bloatit.framework.webprocessor.components.form.HtmlSubmit;
import com.bloatit.framework.webprocessor.components.meta.HtmlElement;
import com.bloatit.framework.webprocessor.components.meta.HtmlMixedText;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.model.MoneyWithdrawal;
import com.bloatit.model.lists.MoneyWithdrawalList;
import com.bloatit.model.managers.MoneyWithdrawalManager;
import com.bloatit.web.linkable.admin.AdminPage;
import com.bloatit.web.pages.master.Breadcrumb;
import com.bloatit.web.pages.master.sidebar.TwoColumnLayout;
import com.bloatit.web.url.AdminHomePageUrl;
import com.bloatit.web.url.MoneyWithdrawalAdminActionUrl;
import com.bloatit.web.url.MoneyWithdrawalAdminPageUrl;

@ParamContainer("admin/withdraw")
public class MoneyWithdrawalAdminPage extends AdminPage {
    private MoneyWithdrawalAdminPageUrl url;

    private static final String FILTER_TREATED = "treated";
    private static final String FILTER_REQUESTED = "requested";
    private static final String FILTER_COMPLETE = "complete";
    private static final String FILTER_CANCELED = "canceled";
    private static final String FILTER_REFUSED = "refused";
    private static final String FILTER_ALL = "all";

    @RequestParam(name = "filter")
    @Optional(FILTER_REQUESTED)
    private final String filter;

    public MoneyWithdrawalAdminPage(MoneyWithdrawalAdminPageUrl url) {
        super(url);
        this.url = url;
        this.filter = url.getFilter();
    }

    @Override
    protected HtmlElement createAdminContent() throws UnauthorizedOperationException {
        HtmlDiv master = new HtmlDiv("padding_box");
        master.add(generateMain());
        return master;
    }

    private HtmlElement generateMain() {
        HtmlDiv master = new HtmlDiv();
        HtmlTitle title = new HtmlTitle(1);
        title.addText(Context.tr("Money withdrawal administration"));
        master.add(title);

        HtmlTitle filterTitle = new HtmlTitle(2);
        filterTitle.addText(Context.tr("Filter withdrawal requests"));
        master.add(filterTitle);

        MoneyWithdrawalAdminPageUrl allUrl = new MoneyWithdrawalAdminPageUrl();
        allUrl.setFilter(FILTER_ALL);
        HtmlLink allLink = allUrl.getHtmlLink(Context.tr("All"));
        if (filter.equals(FILTER_ALL)) {
            allLink.setCssClass("selected");
        }

        MoneyWithdrawalAdminPageUrl requestedUrl = new MoneyWithdrawalAdminPageUrl();
        requestedUrl.setFilter(FILTER_REQUESTED);
        HtmlLink requestedLink = requestedUrl.getHtmlLink(Context.tr("Requested"));
        if (filter.equals(FILTER_REQUESTED)) {
            requestedLink.setCssClass("selected");
        }

        MoneyWithdrawalAdminPageUrl treatedUrl = new MoneyWithdrawalAdminPageUrl();
        treatedUrl.setFilter(FILTER_TREATED);
        HtmlLink treatedLink = treatedUrl.getHtmlLink(Context.tr("Treated"));
        if (filter.equals(FILTER_TREATED)) {
            treatedLink.setCssClass("selected");
        }

        MoneyWithdrawalAdminPageUrl completeUrl = new MoneyWithdrawalAdminPageUrl();
        completeUrl.setFilter(FILTER_COMPLETE);
        HtmlLink completeLink = completeUrl.getHtmlLink(Context.tr("Complete"));
        if (filter.equals(FILTER_COMPLETE)) {
            completeLink.setCssClass("selected");
        }

        MoneyWithdrawalAdminPageUrl canceledUrl = new MoneyWithdrawalAdminPageUrl();
        canceledUrl.setFilter(FILTER_CANCELED);
        HtmlLink canceledLink = canceledUrl.getHtmlLink(Context.tr("Canceled"));
        if (filter.equals(FILTER_CANCELED)) {
            canceledLink.setCssClass("selected");
        }

        MoneyWithdrawalAdminPageUrl refusedUrl = new MoneyWithdrawalAdminPageUrl();
        refusedUrl.setFilter(FILTER_REFUSED);
        HtmlLink refusedLink = refusedUrl.getHtmlLink(Context.tr("Refused"));
        if (filter.equals(FILTER_REFUSED)) {
            refusedLink.setCssClass("selected");
        }

        HtmlMixedText mixed = new HtmlMixedText(Context.tr("Filter: <0::>, <1::>, <2::>, <3::>, <4::>, <5::>"),
                                                allLink,
                                                requestedLink,
                                                treatedLink,
                                                completeLink,
                                                canceledLink,
                                                refusedLink);
        master.add(mixed);

        State state = getState(filter);
        MoneyWithdrawalList list;
        if (state == null) {
            list = MoneyWithdrawalManager.getAll();
        } else {
            list = MoneyWithdrawalManager.getByState(state);
        }

        HtmlSimpleLineTable table = new HtmlSimpleLineTable();
        master.add(table);
        table.addHeader("Author", "Amount", "State", "IBAN", "Select");
        for (MoneyWithdrawal mw : list) {
            MoneyWithdrawalAdminActionUrl targetUrl = new MoneyWithdrawalAdminActionUrl(mw);
            HtmlForm form = null;

            FieldData stateFieldData = targetUrl.getNewStateParameter().pickFieldData();
            HtmlDropDown dd = new HtmlDropDown(stateFieldData.getName());

            switch (mw.getState()) {
                case CANCELED:
                case COMPLETE:
                case REFUSED:
                    break;
                case REQUESTED:
                    form = new HtmlForm(targetUrl.urlString());
                    dd.addDropDownElement(State.TREATED.toString(), "TREATED");
                    dd.addDropDownElement(State.REFUSED.toString(), "REFUSED");
                    form.add(dd);
                    form.add(new HtmlSubmit(Context.tr("Submit")));
                    break;
                case TREATED:
                    form = new HtmlForm(targetUrl.urlString());
                    dd.addDropDownElement(State.COMPLETE.toString(), "COMPLETE");
                    dd.addDropDownElement(State.REFUSED.toString(), "REFUSED");
                    form.add(dd);
                    form.add(new HtmlSubmit(Context.tr("Submit")));
                    break;
            }
            table.addLine(mw.getActor().getDisplayName(), mw.getAmountWithdrawn(), mw.getState(), mw.getIBAN(), form);
        }

        return master;
    }

    private State getState(String filter) {
        if (filter.equals(FILTER_REQUESTED)) {
            return State.REQUESTED;
        } else if (filter.equals(FILTER_TREATED)) {
            return State.TREATED;
        } else if (filter.equals(FILTER_COMPLETE)) {
            return State.COMPLETE;
        } else if (filter.equals(FILTER_CANCELED)) {
            return State.CANCELED;
        } else if (filter.equals(FILTER_REFUSED)) {
            return State.REFUSED;
        }
        return null;
    }

    @Override
    protected String createPageTitle() {
        return "Money withdrawal administration";
    }

    @Override
    protected Breadcrumb createBreadcrumb() {
        Breadcrumb crumb = new Breadcrumb();
        crumb.pushLink(new AdminHomePageUrl().getHtmlLink("admin"));
        crumb.pushLink(new MoneyWithdrawalAdminPageUrl().getHtmlLink("withdraw"));
        return crumb;
    }

    @Override
    public boolean isStable() {
        return false;
    }
}
