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
package com.bloatit.web.linkable.offer;

import java.math.BigDecimal;

import com.bloatit.data.DaoTeamRight.UserTeamRight;
import com.bloatit.framework.exceptions.highlevel.ShallNotPassException;
import com.bloatit.framework.utils.datetime.DateUtils;
import com.bloatit.framework.utils.i18n.DateLocale;
import com.bloatit.framework.utils.i18n.Language;
import com.bloatit.framework.webprocessor.annotations.MaxConstraint;
import com.bloatit.framework.webprocessor.annotations.MinConstraint;
import com.bloatit.framework.webprocessor.annotations.NonOptional;
import com.bloatit.framework.webprocessor.annotations.Optional;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.RequestParam.Role;
import com.bloatit.framework.webprocessor.annotations.tr;
import com.bloatit.framework.webprocessor.components.form.FormComment;
import com.bloatit.framework.webprocessor.components.form.FormField;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.framework.webprocessor.url.Url;
import com.bloatit.model.Feature;
import com.bloatit.model.Member;
import com.bloatit.model.Milestone;
import com.bloatit.model.Offer;
import com.bloatit.model.Team;
import com.bloatit.model.right.UnauthorizedOperationException;
import com.bloatit.web.linkable.features.FeatureTabPane.FeatureTabKey;
import com.bloatit.web.linkable.usercontent.UserContentAction;
import com.bloatit.web.url.FeaturePageUrl;
import com.bloatit.web.url.MakeOfferPageUrl;
import com.bloatit.web.url.OfferActionUrl;

/**
 * Class that will create a new offer based on data received from a form.
 */
@ParamContainer("offer/docreate")
public final class OfferAction extends UserContentAction {

    @RequestParam(role = Role.GET, message = @tr("The target feature is mandatory to make an offer."))
    private final Feature feature;

    @RequestParam(role = Role.GET)
    @Optional
    private final Offer draftOffer;

    @RequestParam(role = Role.POST, message = @tr("Invalid value for price field."))
    @NonOptional(@tr("You must set a price to your offer."))
    @MinConstraint(min = 1, message = @tr("The price must be greater to %constraint%."))
    @FormField(label = @tr("Offer price"))
    @FormComment(@tr("The price is in euros (€) and can't contains cents."))
    private final BigDecimal price;

    @RequestParam(role = Role.POST)
    @NonOptional(@tr("You must set an expiration date."))
    @FormField(label = @tr("Release date"))
    @FormComment(@tr("You will have to release this feature before the release date."))
    private final DateLocale expiryDate;

    @RequestParam(role = Role.POST)
    @NonOptional(@tr("You must add a description to your offer."))
    @MaxConstraint(max = 800000, message = @tr("The length of the description must be smaller than %constraint% characters."))
    @FormField(label = @tr("Description"), isShort = false)
    @FormComment(@tr("Describe your offer. This description must be accurate because it will be used to validate the conformity at the end of the development."))
    private final String description;

    @RequestParam(role = Role.POST)
    @NonOptional(@tr("You must add a license to your offer."))
    @FormField(label = @tr("License"))
    private final String license;

    @RequestParam(role = Role.POST, suggestedValue = "7")
    @Optional("7")
    @MinConstraint(min = 1, message = @tr("The validation time must be greater to %constraint%."))
    @FormField(label = @tr("Days before validation"))
    @FormComment(@tr("The number of days to wait before this offer is can be validated. During this time users can add bugs un the bug tracker. Fatal bugs have to be closed before the validation."))
    private final Integer daysBeforeValidation;

    @Optional("100")
    @RequestParam(role = Role.POST, suggestedValue = "100")
    @MinConstraint(min = 0, message = @tr("''%paramName%'' is a percent, and must be greater or equal to %constraint%."))
    @MaxConstraint(max = 100, message = @tr("''%paramName%'' is a percent, and must be lesser or equal to %constraint%."))
    @FormField(label = @tr("Percent gained when no FATAL bugs"))
    @FormComment(@tr("If you want to add some warranty to the contributor you can say that you want to gain less than 100% "
            + "of the amount on this feature request when all the FATAL bugs are closed. "
            + "The money left will be transfered when all the MAJOR bugs are closed. If you specify this field, you have to specify the next one on MAJOR bug percent. "
            + "By default, all the money on this feature request is transfered when all the FATAL bugs are closed."))
    private final Integer percentFatal;

    @RequestParam(role = Role.POST, suggestedValue = "0")
    @Optional("0")
    @MinConstraint(min = 0, message = @tr("''%paramName%'' is a percent, and must be greater or equal to %constraint%."))
    @MaxConstraint(max = 100, message = @tr("''%paramName%'' is a percent, and must be lesser or equal to %constraint%."))
    @FormField(label = @tr("Percent gained when no MAJOR bugs"))
    @FormComment(@tr("If you specified a value for the 'FATAL bugs percent', you have to also specify one for the MAJOR bugs. "
            + "You can say that you want to gain less than 100% of the amount on this offer when all the MAJOR bugs are closed. "
            + "The money left will be transfered when all the MINOR bugs are closed. Make sure that (FATAL percent + MAJOR percent) <= 100."))
    private final Integer percentMajor;

    @RequestParam(role = Role.POST, suggestedValue = "true")
    private final Boolean isFinished;

    private final OfferActionUrl url;

    public OfferAction(final OfferActionUrl url) {
        super(url, UserTeamRight.TALK);
        this.url = url;
        this.description = url.getDescription();
        this.license = url.getLicense();
        this.expiryDate = url.getExpiryDate();
        this.price = url.getPrice();
        this.feature = url.getFeature();
        this.draftOffer = url.getDraftOffer();
        this.daysBeforeValidation = url.getDaysBeforeValidation();
        this.percentFatal = url.getPercentFatal();
        this.percentMajor = url.getPercentMajor();
        this.isFinished = url.getIsFinished() != null && url.getIsFinished();
    }

    @Override
    public Url doDoProcessRestricted(final Member me, final Team asTeam) {

        Offer constructingOffer;
        try {
            Milestone constructingMilestone;
            if (draftOffer == null) {
                constructingOffer = feature.addOffer(price,
                                                     description,
                                                     license,
                                                     Language.fromLocale(getLocale()),
                                                     expiryDate.getJavaDate(),
                                                     daysBeforeValidation * DateUtils.SECOND_PER_DAY);
                constructingMilestone = constructingOffer.getMilestones().iterator().next();
            } else {
                constructingOffer = draftOffer;
                constructingMilestone = draftOffer.addMilestone(price,
                                                                description,
                                                                Language.fromLocale(getLocale()),
                                                                expiryDate.getJavaDate(),
                                                                daysBeforeValidation * DateUtils.SECOND_PER_DAY);
            }
            if (percentFatal != null && percentMajor != null) {
                constructingMilestone.updateMajorFatalPercent(percentFatal, percentMajor);
            }
            if (isFinished) {
                constructingOffer.setDraftFinished();
                final FeaturePageUrl featurePageUrl = new FeaturePageUrl(feature, FeatureTabKey.offers);
                return featurePageUrl;
            }

        } catch (final UnauthorizedOperationException e) {
            Context.getSession().notifyError(Context.tr("Error creating an offer. Please notify us."));
            throw new ShallNotPassException("Error creating an offer", e);
        }

        final MakeOfferPageUrl returnUrl = new MakeOfferPageUrl(feature);
        returnUrl.setOffer(constructingOffer);
        return returnUrl;
    }

    @Override
    protected Url checkRightsAndEverything(final Member me) {

        boolean everythingIsRight = true;

        if ((percentFatal != null && percentMajor == null) || (percentFatal == null && percentMajor != null)) {
            session.notifyWarning(Context.tr("You have to specify both the Major and Fatal percent."));
            url.getPercentMajorParameter().addErrorMessage(Context.tr("You have to specify both the Major and Fatal percent."));
            url.getPercentFatalParameter().addErrorMessage(Context.tr("You have to specify both the Major and Fatal percent."));
            everythingIsRight = false;
        }
        if (percentFatal != null && percentFatal + percentMajor > 100) {
            session.notifyWarning(Context.tr("Major + Fatal percent cannot be > 100 !!"));
            url.getPercentMajorParameter().addErrorMessage(Context.tr("Major + Fatal percent cannot be > 100 !!"));
            url.getPercentFatalParameter().addErrorMessage(Context.tr("Major + Fatal percent cannot be > 100 !!"));
            everythingIsRight = false;
        }
        if (draftOffer != null && !draftOffer.isDraft()) {
            session.notifyWarning(Context.tr("The specified offer is not modifiable. You cannot add a lot in it."));
            everythingIsRight = false;
        }
        if (!expiryDate.isFuture()) {
            session.notifyWarning(Context.tr("The date must be in the future."));
            url.getExpiryDateParameter().addErrorMessage(Context.tr("The date must be in the future."));
            everythingIsRight = false;
        }

        if (!everythingIsRight) {
            return new MakeOfferPageUrl(feature);
        }

        return NO_ERROR;
    }

    @Override
    protected Url doProcessErrors() {
        if (feature != null) {
            transmitParameters();
            final MakeOfferPageUrl redirectUrl = new MakeOfferPageUrl(feature);
            redirectUrl.setOffer(draftOffer);
            return redirectUrl;
        }
        return session.pickPreferredPage();
    }

    @Override
    protected String getRefusalReason() {
        return Context.tr("You must be logged to make an offer.");
    }

    @Override
    protected void doTransmitParameters() {
        session.addParameter(url.getDescriptionParameter());
        session.addParameter(url.getExpiryDateParameter());
        session.addParameter(url.getPriceParameter());
        session.addParameter(url.getDaysBeforeValidationParameter());
        session.addParameter(url.getPercentFatalParameter());
        session.addParameter(url.getPercentMajorParameter());
        session.addParameter(url.getIsFinishedParameter());
        session.addParameter(url.getLicenseParameter());
    }

    @Override
    protected boolean verifyFile(final String filename) {
        return true;
    }
}
