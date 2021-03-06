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
package com.bloatit.web.linkable.contribution;

import static com.bloatit.framework.webprocessor.context.Context.tr;

import java.io.IOException;
import java.math.BigDecimal;

import com.bloatit.common.Log;
import com.bloatit.common.TemplateFile;
import com.bloatit.data.DaoTeamRight.UserTeamRight;
import com.bloatit.framework.exceptions.highlevel.BadProgrammerException;
import com.bloatit.framework.exceptions.lowlevel.RedirectException;
import com.bloatit.framework.utils.RandomString;
import com.bloatit.framework.webprocessor.annotations.NonOptional;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.RequestParam.Role;
import com.bloatit.framework.webprocessor.annotations.tr;
import com.bloatit.framework.webprocessor.components.HtmlDiv;
import com.bloatit.framework.webprocessor.components.HtmlSpan;
import com.bloatit.framework.webprocessor.components.HtmlTitleBlock;
import com.bloatit.framework.webprocessor.components.advanced.HtmlScript;
import com.bloatit.framework.webprocessor.components.form.FormBuilder;
import com.bloatit.framework.webprocessor.components.form.HtmlMoneyField;
import com.bloatit.framework.webprocessor.components.form.HtmlSubmit;
import com.bloatit.framework.webprocessor.components.form.HtmlTextArea;
import com.bloatit.framework.webprocessor.components.meta.HtmlElement;
import com.bloatit.framework.webprocessor.components.meta.HtmlMixedText;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.model.BankTransaction;
import com.bloatit.model.Feature;
import com.bloatit.model.right.AuthToken;
import com.bloatit.model.right.UnauthorizedOperationException;
import com.bloatit.web.components.HtmlElveosForm;
import com.bloatit.web.components.SideBarFeatureBlock;
import com.bloatit.web.components.SidebarMarkdownHelp;
import com.bloatit.web.linkable.features.FeaturePage;
import com.bloatit.web.linkable.master.Breadcrumb;
import com.bloatit.web.linkable.master.ElveosPage;
import com.bloatit.web.linkable.master.sidebar.TwoColumnLayout;
import com.bloatit.web.linkable.usercontent.AsTeamField;
import com.bloatit.web.url.CheckContributeActionUrl;
import com.bloatit.web.url.ContributePageUrl;

/**
 * A page that hosts the form used to contribute on a Feature
 */
@ParamContainer("contribute/process/%process%")
public final class ContributePage extends ElveosPage {
    @RequestParam(message = @tr("The process is closed, expired, missing or invalid."), role = Role.PAGENAME)
    @NonOptional(@tr("The process is closed, expired, missing or invalid."))
    private final ContributionProcess process;

    private final ContributePageUrl url;

    public ContributePage(final ContributePageUrl url) {
        super(url);
        this.url = url;
        process = url.getProcess();
    }

    @Override
    protected HtmlElement createBodyContent() throws RedirectException {
        final TwoColumnLayout layout = new TwoColumnLayout(true, url);
        layout.addLeft(generateContributeForm());
        layout.addRight(new SideBarFeatureBlock(process.getFeature()));
        layout.addRight(new SidebarMarkdownHelp());

        return layout;
    }

    private HtmlElement generateContributeForm() {
        final CheckContributeActionUrl targetUrl = new CheckContributeActionUrl(getSession().getShortKey(), process);
        final HtmlElveosForm form = new HtmlElveosForm(targetUrl.urlString());
        form.setCssClass("contribution_page");
        final FormBuilder ftool = new FormBuilder(CheckContributeAction.class, targetUrl);

        // Input field : choose amount
        final HtmlMoneyField moneyField = new HtmlMoneyField(targetUrl.getAmountParameter().getName());
        ftool.add(form, moneyField);
        final String defaultValueIfNeeded = ftool.setDefaultValueIfNeeded(moneyField, process.getAmount().toPlainString());
        BigDecimal contributionValue = process.getAmount();
        if (defaultValueIfNeeded != null) {
            contributionValue = new BigDecimal(defaultValueIfNeeded);
        } else if (contributionValue == null) {
            contributionValue = BigDecimal.ZERO;
        }

        if (AuthToken.isAuthenticated()) {
            // Input field : As team
            final AsTeamField teamField = new AsTeamField(targetUrl,
                                                          AuthToken.getMember(),
                                                          UserTeamRight.BANK,
                                                          tr("In the name of"),
                                                          tr("Talk in the name of this team and use its money to make a contribution."));
            form.addAsTeamField(teamField);
            if (process.getTeam() != null) {
                teamField.getTeamInput().setDefaultValue(process.getTeam().getId().toString());
            }
        }

        // Js quick uotation
        try {
            if (process.getAccountChargingAmount().compareTo(BigDecimal.ZERO) == 0
                    && (!AuthToken.isAuthenticated() || AuthToken.getMember().getInternalAccount().getAmount().compareTo(BigDecimal.ZERO) == 0)) {

                final HtmlDiv quickQuotationBlock = new HtmlDiv("quick_quotation_block");

                final HtmlSpan targetField = new HtmlSpan("", "target_field");
                targetField.addText(BankTransaction.computateAmountToPay(contributionValue).toPlainString() + " €");
                quickQuotationBlock.add(new HtmlMixedText(Context.tr("You will have to pay <0::>."), targetField));
                form.add(quickQuotationBlock);

                final HtmlScript quotationUpdateScript = new HtmlScript();

                final TemplateFile quotationUpdateScriptTemplate = new TemplateFile("quick_quotation.js");
                final RandomString rng = new RandomString(8);
                moneyField.setId(rng.nextString());

                quotationUpdateScriptTemplate.addNamedParameter("target_field", targetField.getId());
                quotationUpdateScriptTemplate.addNamedParameter("charge_field_id", moneyField.getId());
                quotationUpdateScriptTemplate.addNamedParameter("commission_variable_rate", String.valueOf(BankTransaction.COMMISSION_VARIABLE_RATE));
                quotationUpdateScriptTemplate.addNamedParameter("commission_fix_rate", String.valueOf(BankTransaction.COMMISSION_FIX_RATE));

                try {
                    quotationUpdateScript.append(quotationUpdateScriptTemplate.getContent(null));
                } catch (final IOException e) {
                    Log.web().error("Fail to generate quick quotation update script", e);
                }

                targetField.add(quotationUpdateScript);

            }
        } catch (final UnauthorizedOperationException e) {
            throw new BadProgrammerException("Fail to access to the internal account value of a user");
        }

        // Input field : comment
        final HtmlTextArea commentInput = new HtmlTextArea(targetUrl.getCommentParameter().getName(), 3, 60);
        ftool.add(form, commentInput);
        String suggestedCommentValue = ftool.setDefaultValueIfNeeded(commentInput, process.getComment());
        if (suggestedCommentValue == null) {
            suggestedCommentValue = process.getComment();
        }

        form.addSubmit(new HtmlSubmit(tr("Contribute")));

        final HtmlTitleBlock contribTitle = new HtmlTitleBlock(tr("Contribute"), 1);
        contribTitle.add(form);

        final HtmlDiv group = new HtmlDiv();
        group.add(contribTitle);

        return group;
    }

    @Override
    protected String createPageTitle() {
        return tr("Contribute to a feature");
    }

    @Override
    public boolean isStable() {
        return false;
    }

    @Override
    protected Breadcrumb createBreadcrumb() {
        return ContributePage.generateBreadcrumb(process.getFeature(), process);
    }

    private static Breadcrumb generateBreadcrumb(final Feature feature, final ContributionProcess process) {
        final Breadcrumb breadcrumb = FeaturePage.generateBreadcrumbContributions(feature);
        breadcrumb.pushLink(new ContributePageUrl(process).getHtmlLink(tr("Contribute")));
        return breadcrumb;
    }

}
