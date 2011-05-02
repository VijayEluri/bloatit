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
package com.bloatit.web.linkable.features;

import static com.bloatit.framework.webprocessor.context.Context.tr;

import java.math.BigDecimal;

import com.bloatit.common.Log;
import com.bloatit.data.DaoMilestone.MilestoneState;
import com.bloatit.data.queries.EmptyPageIterable;
import com.bloatit.framework.exceptions.highlevel.ShallNotPassException;
import com.bloatit.framework.exceptions.lowlevel.UnauthorizedOperationException;
import com.bloatit.framework.utils.PageIterable;
import com.bloatit.framework.utils.datetime.DateUtils;
import com.bloatit.framework.utils.datetime.TimeRenderer;
import com.bloatit.framework.utils.datetime.TimeRenderer.TimeBase;
import com.bloatit.framework.utils.i18n.CurrencyLocale;
import com.bloatit.framework.utils.i18n.DateLocale.FormatStyle;
import com.bloatit.framework.webprocessor.components.HtmlDiv;
import com.bloatit.framework.webprocessor.components.HtmlLink;
import com.bloatit.framework.webprocessor.components.HtmlList;
import com.bloatit.framework.webprocessor.components.HtmlListItem;
import com.bloatit.framework.webprocessor.components.HtmlParagraph;
import com.bloatit.framework.webprocessor.components.HtmlSpan;
import com.bloatit.framework.webprocessor.components.HtmlTitle;
import com.bloatit.framework.webprocessor.components.PlaceHolderElement;
import com.bloatit.framework.webprocessor.components.javascript.JsShowHide;
import com.bloatit.framework.webprocessor.components.meta.HtmlElement;
import com.bloatit.framework.webprocessor.components.meta.HtmlMixedText;
import com.bloatit.framework.webprocessor.components.meta.XmlNode;
import com.bloatit.framework.webprocessor.components.meta.XmlText;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.model.Feature;
import com.bloatit.model.Milestone;
import com.bloatit.model.Offer;
import com.bloatit.model.Release;
import com.bloatit.model.feature.FeatureImplementation;
import com.bloatit.web.HtmlTools;
import com.bloatit.web.components.HtmlProgressBar;
import com.bloatit.web.linkable.members.MembersTools;
import com.bloatit.web.pages.master.HtmlDefineParagraph;
import com.bloatit.web.url.AddReleasePageUrl;
import com.bloatit.web.url.MakeOfferPageUrl;
import com.bloatit.web.url.MemberPageUrl;
import com.bloatit.web.url.PopularityVoteActionUrl;
import com.bloatit.web.url.ReleasePageUrl;
import com.bloatit.web.url.TeamPageUrl;

public class FeatureOfferListComponent extends HtmlDiv {
    protected FeatureOfferListComponent(final Feature feature) {
        super();
        try {

            PageIterable<Offer> offers = new EmptyPageIterable<Offer>();
            offers = feature.getOffers();
            int nbUnselected = offers.size();

            final Offer selectedOffer = feature.getSelectedOffer();
            if (selectedOffer != null) {
                nbUnselected--;
            }

            final HtmlDiv offersBlock = new HtmlDiv("offers_block");

            switch (feature.getFeatureState()) {
                case PENDING: {
                    offersBlock.add(new HtmlTitle(Context.tr("No offer"), 1));
                    final BicolumnOfferBlock block = new BicolumnOfferBlock(true);
                    offersBlock.add(block);
                    block.addInLeftColumn(new HtmlParagraph(tr("There is not yet offer to develop this feature. The first offer is selected by default.")));

                    final HtmlLink link = new MakeOfferPageUrl(feature).getHtmlLink(Context.tr("Make an offer"));
                    link.setCssClass("button");

                    final HtmlDiv noOffer = new HtmlDiv("no_offer_block");
                    {
                        noOffer.add(link);
                    }
                    block.addInRightColumn(noOffer);

                }
                    break;
                case PREPARING:
                    offersBlock.add(new HtmlTitle(Context.tr("Selected offer"), 1));

                    // Selected
                    BicolumnOfferBlock block = new BicolumnOfferBlock(true);
                    offersBlock.add(block);

                    // Generating the left column
                    block.addInLeftColumn(new HtmlParagraph(tr("The selected offer is the one with the more popularity.")));
                    if (selectedOffer != null) {
                        if (feature.getValidationDate() != null && DateUtils.isInTheFuture(feature.getValidationDate())) {
                            final TimeRenderer renderer = new TimeRenderer(DateUtils.elapsed(DateUtils.now(), feature.getValidationDate()));

                            final BigDecimal amountLeft = selectedOffer.getAmount().subtract(feature.getContribution());

                            if (amountLeft.compareTo(BigDecimal.ZERO) > 0) {
                                final CurrencyLocale currency = Context.getLocalizator().getCurrency(amountLeft);
                                HtmlSpan timeSpan = new HtmlSpan("bold");
                                timeSpan.addText(renderer.getTimeString());
                                HtmlMixedText timeToValid = new HtmlMixedText(tr("This offer will be validated in about <0::>. After this time, the offer will go into development as soon as the requested amount is available ({0} left).",
                                                                                 currency.toString()),
                                                                              timeSpan);
                                HtmlParagraph element = new HtmlParagraph(timeToValid);
                                block.addInLeftColumn(element);
                            } else {
                                HtmlSpan timeSpan = new HtmlSpan("bold");
                                timeSpan.addText(renderer.getTimeString());
                                HtmlMixedText timeToValid = new HtmlMixedText("This offer will go into development in about <0::>.", timeSpan);
                                block.addInLeftColumn(timeToValid);
                            }
                        } else {
                            final BigDecimal amountLeft = feature.getSelectedOffer().getAmount().subtract(feature.getContribution());
                            final CurrencyLocale currency = Context.getLocalizator().getCurrency(amountLeft);
                            block.addInLeftColumn(new HtmlParagraph(tr("This offer is validated and will go into development as soon as the requested amount is available ({0} left).",
                                                                       currency.toString())));
                        }
                        // Generating the right column
                        block.addInRightColumn(new OfferBlock(selectedOffer, true));
                    } else {
                        block.addInRightColumn(new HtmlParagraph(tr("No selected offer. The last selected offer has been voted down and is no more selected."),
                                                                 "no_selected_offer_para"));
                    }

                    // UnSelected
                    offersBlock.add(new HtmlTitle(Context.trn("Unselected offer ({0})", "Unselected offers ({0})", nbUnselected, nbUnselected), 1));
                    final BicolumnOfferBlock unselectedBlock = new BicolumnOfferBlock(true);
                    offersBlock.add(unselectedBlock);
                    unselectedBlock.addInLeftColumn(new MakeOfferPageUrl(feature).getHtmlLink(tr("Make a concurrent offer")));
                    unselectedBlock.addInLeftColumn(new HtmlParagraph("The concurrent offers must be voted enough to become the selected offer."));

                    for (final Offer offer : offers) {
                        if (offer != selectedOffer) {
                            unselectedBlock.addInRightColumn(new OfferBlock(offer, false));
                        }
                    }
                    break;
                case DEVELOPPING:
                    offersBlock.add(new HtmlTitle(Context.tr("Offer in development"), 1));
                    offersBlock.add(block = new BicolumnOfferBlock(true));
                    block.addInLeftColumn(new HtmlParagraph(tr("This offer is in development. You can discuss about it in the comments.")));
                    if (selectedOffer != null && selectedOffer.hasRelease()) {
                        block.addInLeftColumn(new HtmlParagraph(tr("Test the last release and report bugs.")));
                    }
                    block.addInRightColumn(new OfferBlock(selectedOffer, true));

                    generateOldOffersList(offers, nbUnselected, selectedOffer, offersBlock);

                    break;
                case FINISHED:
                    offersBlock.add(new HtmlTitle(Context.tr("Finished offer"), 1));
                    offersBlock.add(block = new BicolumnOfferBlock(true));
                    block.addInLeftColumn(new HtmlParagraph(tr("This offer is finished.")));
                    block.addInRightColumn(new OfferBlock(selectedOffer, true));

                    generateOldOffersList(offers, nbUnselected, selectedOffer, offersBlock);
                    break;
                case DISCARDED:
                    offersBlock.add(new HtmlTitle(Context.tr("Feature discarded ..."), 1));
                    break;
                default:
                    break;
            }
            add(offersBlock);

        } catch (final UnauthorizedOperationException e) {
            // No right no current offer.
            Context.getSession().notifyError(Context.tr("An error prevented us from displaying selected offer. Please notify us."));
            throw new ShallNotPassException("User cannot access selected offer", e);
        }
    }

    private void
            generateOldOffersList(final PageIterable<Offer> offers, final int nbUnselected, final Offer selectedOffer, final HtmlDiv offersBlock)
                    throws UnauthorizedOperationException {
        // UnSelected
        offersBlock.add(new HtmlTitle(Context.trn("Old offer ({0})", "Old offers ({0})", nbUnselected, nbUnselected), 1));
        final BicolumnOfferBlock unselectedBlock = new BicolumnOfferBlock(true);
        offersBlock.add(unselectedBlock);
        unselectedBlock.addInLeftColumn(new HtmlParagraph(Context.tr("These offers have not been selected and will never be developed.")));

        for (final Offer offer : offers) {
            if (offer != selectedOffer) {
                unselectedBlock.addInRightColumn(new OfferBlock(offer, false));
            }
        }
    }

    private static class BicolumnOfferBlock extends HtmlDiv {

        private final PlaceHolderElement leftColumn = new PlaceHolderElement();
        private final PlaceHolderElement rightColumn = new PlaceHolderElement();

        public BicolumnOfferBlock(final boolean isSelected) {
            super("offer_type_block");
            HtmlDiv divSelected;
            if (isSelected) {
                divSelected = new HtmlDiv("offer_selected_description");
            } else {
                divSelected = new HtmlDiv("offer_unselected_description");
            }
            add(new HtmlDiv("offer_type_left_column").add(divSelected.add(leftColumn)));
            add(new HtmlDiv("offer_type_right_column").add(rightColumn));
        }

        public void addInLeftColumn(final HtmlElement elment) {
            leftColumn.add(elment);
        }

        public void addInRightColumn(final HtmlElement elment) {
            rightColumn.add(elment);
        }
    }

    public static final class OfferBlock extends HtmlDiv {

        private final Offer offer;

        public OfferBlock(final Offer offer, final boolean selected) throws UnauthorizedOperationException {
            super((selected ? "offer_selected_block" : "offer_unselected_block"));
            this.offer = offer;

            final HtmlDiv offerTopBlock = new HtmlDiv("offer_top_block");
            {
                final HtmlDiv offerLeftTopColumn = new HtmlDiv("offer_left_top_column");
                {
                    offerLeftTopColumn.add(MembersTools.getMemberAvatar(offer.getAuthor()));
                }
                offerTopBlock.add(offerLeftTopColumn);

                final HtmlDiv offerRightTopColumn = new HtmlDiv("offer_right_top_column");
                {
                    final HtmlDiv offerPriceBlock = new HtmlDiv("offer_price_block");
                    {
                        final HtmlSpan priceLabel = new HtmlSpan("offer_block_label");
                        priceLabel.addText(Context.tr("Total price: "));
                        offerPriceBlock.add(priceLabel);

                        final HtmlSpan price = new HtmlSpan("offer_block_price");
                        price.addText(Context.getLocalizator().getCurrency(offer.getAmount()).getSimpleEuroString());
                        offerPriceBlock.add(price);
                    }
                    offerRightTopColumn.add(offerPriceBlock);

                    final HtmlParagraph authorPara = new HtmlParagraph();
                    authorPara.setCssClass("offer_block_para");
                    {
                        final HtmlSpan authorLabel = new HtmlSpan("offer_block_label");
                        authorLabel.addText(Context.tr("Author: "));
                        authorPara.add(authorLabel);

                        HtmlLink author = null;
                        author = new MemberPageUrl(offer.getMember()).getHtmlLink(offer.getMember().getDisplayName());
                        author.setCssClass("offer_block_author");
                        authorPara.add(author);
                        if (offer.getAsTeam() != null) {
                            authorPara.addText(Context.tr(" on the behalf of "));
                            authorPara.add(new TeamPageUrl(offer.getAsTeam()).getHtmlLink(offer.getAsTeam().getDisplayName()));
                        }
                    }
                    offerRightTopColumn.add(authorPara);

                    final HtmlDiv progressPara = new HtmlDiv("offer_block_para");
                    {
                        final HtmlSpan progressLabel = new HtmlSpan("offer_block_label");
                        progressLabel.addText(Context.tr("Funding: "));
                        progressPara.add(progressLabel);

                        final int progression = (int) Math.floor(offer.getProgression());
                        final HtmlSpan progress = new HtmlSpan("offer_block_progress");
                        progress.addText(Context.tr("{0} %", String.valueOf(progression)));
                        progressPara.add(progress);

                        int cappedProgressValue = progression;
                        if (cappedProgressValue > FeatureImplementation.PROGRESSION_PERCENT) {
                            cappedProgressValue = FeatureImplementation.PROGRESSION_PERCENT;
                        }

                        final HtmlProgressBar progressBar = new HtmlProgressBar(cappedProgressValue);
                        progressPara.add(progressBar);

                    }
                    offerRightTopColumn.add(progressPara);
                }
                offerTopBlock.add(offerRightTopColumn);
            }
            add(offerTopBlock);

            // TODO: choose to display the title or not
            final HtmlDiv offerBottomBlock = new HtmlDiv("offer_bottom_block");
            {
                final HtmlDiv offerLeftBottomColumn = new HtmlDiv("offer_left_bottom_column");
                {
                    offerLeftBottomColumn.add(generatePopularityBlock());
                }
                offerBottomBlock.add(offerLeftBottomColumn);

                final HtmlDiv offerRightBottomColumn = new HtmlDiv("offer_right_bottom_column");
                {
                    // Lots
                    final PageIterable<Milestone> lots = offer.getMilestones();
                    if (lots.size() == 1) {
                        final Milestone lot = lots.iterator().next();

                        final HtmlParagraph datePara = new HtmlParagraph();
                        datePara.setCssClass("offer_block_para");
                        {
                            final HtmlSpan dateLabel = new HtmlSpan("offer_block_label");
                            dateLabel.addText(Context.tr("Delivery Date: "));
                            datePara.add(dateLabel);

                            // TODO: use scheduled e date
                            final HtmlSpan date = new HtmlSpan("offer_block_date");
                            date.addText(Context.getLocalizator().getDate(lot.getExpirationDate()).toString(FormatStyle.MEDIUM));
                            datePara.add(date);
                        }
                        offerRightBottomColumn.add(datePara);

                        final HtmlParagraph description = new HtmlParagraph();
                        {
                            final HtmlSpan descriptionLabel = new HtmlSpan("offer_block_label");
                            descriptionLabel.addText(Context.tr("Offer's description: "));
                            description.add(descriptionLabel);
                            description.add(new XmlText("<br />"));
                            description.addText(lot.getDescription());
                        }
                        offerRightBottomColumn.add(description);

                        generateAddReleaseLink(lot, offerRightBottomColumn);

                        generateReleaseList(lot, offerRightBottomColumn);

                        // Validation details
                        generateValidationDetails(lot, offerRightBottomColumn);

                    } else {
                        int i = 0;
                        for (final Milestone lot : lots) {
                            i++;
                            final HtmlDiv lotBlock = new HtmlDiv("offer_lot_block");
                            {
                                final HtmlDiv offerLotPriceBlock = new HtmlDiv("offer_price_block");
                                {
                                    final HtmlSpan priceLabel = new HtmlSpan("offer_block_label");
                                    priceLabel.addText(Context.tr("Price: "));
                                    offerLotPriceBlock.add(priceLabel);

                                    final HtmlSpan price = new HtmlSpan("offer_block_price_lot");
                                    price.addText(Context.getLocalizator().getCurrency(lot.getAmount()).getSimpleEuroString());
                                    offerLotPriceBlock.add(price);
                                }
                                lotBlock.add(offerLotPriceBlock);

                                final HtmlTitle lotTitle = new HtmlTitle(Context.tr("Lot {0} - ", i) + getLotState(lot), 2);
                                lotBlock.add(lotTitle);

                                final HtmlParagraph datePara = new HtmlParagraph();
                                datePara.setCssClass("offer_block_para");
                                {
                                    final HtmlSpan dateLabel = new HtmlSpan("offer_block_label");
                                    dateLabel.addText(Context.tr("Delivery Date: "));
                                    datePara.add(dateLabel);

                                    // TODO: use scheduled release date
                                    final HtmlSpan date = new HtmlSpan("offer_block_date");
                                    date.addText(Context.getLocalizator().getDate(lot.getExpirationDate()).toString(FormatStyle.MEDIUM));
                                    datePara.add(date);
                                }
                                lotBlock.add(datePara);

                                final HtmlParagraph description = new HtmlParagraph();
                                description.addText(lot.getDescription());
                                lotBlock.add(description);

                                generateAddReleaseLink(lot, lotBlock);

                                generateReleaseList(lot, lotBlock);

                                // Validation details
                                generateValidationDetails(lot, lotBlock);
                            }
                            offerRightBottomColumn.add(lotBlock);
                        }
                    }

                }
                offerBottomBlock.add(offerRightBottomColumn);
            }
            add(offerBottomBlock);

        }

        private void generateReleaseList(final Milestone lot, final HtmlDiv lotBlock) {
            final PageIterable<Release> releases = lot.getReleases();
            if (releases.size() > 0) {
                lotBlock.add(new HtmlParagraph(tr("Releases:")));
                final HtmlList list = new HtmlList();
                for (final Release release : releases) {
                    final String date = Context.getLocalizator().getDate(release.getCreationDate()).toString(FormatStyle.MEDIUM);
                    final HtmlLink releaseLink = new ReleasePageUrl(release).getHtmlLink(release.getVersion());
                    list.add(new HtmlListItem(new HtmlMixedText("<0::> (<1::>)", releaseLink, new HtmlSpan().addText(date))));
                }
                lotBlock.add(list);
            }
        }

        private void generateValidationDetails(final Milestone lot, final HtmlDiv lotBlock) {

            final JsShowHide showHideValidationDetails = new JsShowHide(false);
            showHideValidationDetails.setHasFallback(false);

            final HtmlParagraph showHideLink = new HtmlParagraph(Context.tr("show validation details"));
            showHideLink.setCssClass("fake_link");
            showHideValidationDetails.addActuator(showHideLink);
            lotBlock.add(showHideLink);

            final HtmlDiv validationDetailsDiv = new HtmlDiv();

            final HtmlDefineParagraph timeBeforeValidationPara = new HtmlDefineParagraph(Context.tr("Minimun time for validation: "),
                                                                                         new TimeRenderer(lot.getSecondBeforeValidation()
                                                                                                 * DateUtils.MILLISECOND_PER_SECOND).renderRange(TimeBase.DAY,
                                                                                                                                                 FormatStyle.MEDIUM));
            validationDetailsDiv.add(timeBeforeValidationPara);

            final HtmlDefineParagraph fatalBugPourcentPara = new HtmlDefineParagraph(Context.tr("Payment when no fatal bug: "),
                                                                                     String.valueOf(lot.getFatalBugsPercent()) + "%");
            validationDetailsDiv.add(fatalBugPourcentPara);

            final HtmlDefineParagraph majorBugPourcentPara = new HtmlDefineParagraph(Context.tr("Payment when no fatal bug: "),
                                                                                     String.valueOf(lot.getMajorBugsPercent()) + "%");
            validationDetailsDiv.add(majorBugPourcentPara);

            final HtmlDefineParagraph minorBugPourcentPara = new HtmlDefineParagraph(Context.tr("Payment when no minor bug: "),
                                                                                     String.valueOf(lot.getMinorBugsPercent()) + "%");
            validationDetailsDiv.add(minorBugPourcentPara);

            lotBlock.add(validationDetailsDiv);

            showHideValidationDetails.addListener(validationDetailsDiv);
            showHideValidationDetails.apply();
        }

        private void generateAddReleaseLink(final Milestone lot, final HtmlDiv lotBlock) throws UnauthorizedOperationException {
            if (isDeveloper() && (lot.getMilestoneState() == MilestoneState.DEVELOPING || lot.getMilestoneState() == MilestoneState.UAT)) {
                final HtmlLink link = new HtmlLink(new AddReleasePageUrl(lot).urlString(), tr("Add a release"));
                link.setCssClass("button");
                lotBlock.add(link);
            }
        }

        private boolean isDeveloper() throws UnauthorizedOperationException {
            return Context.getSession().isLogged() && offer.getFeature().getSelectedOffer() != null
                    && offer.getFeature().getSelectedOffer().canTalkAs();
        }

        private String getLotState(final Milestone lot) {
            switch (lot.getMilestoneState()) {
                case PENDING:
                    return "";
                case DEVELOPING:
                    return tr("Developing");
                case UAT:
                    return tr("Released");
                case VALIDATED:
                    return tr("Validated");
                case CANCELED:
                    return tr("Canceled");
                default:
                    break;
            }
            Log.web().fatal("Lot not found in getLotState ! this is an implementation bug");
            return "";
        }

        private XmlNode generatePopularityBlock() {
            final HtmlDiv offerSummaryPopularity = new HtmlDiv("offer_popularity");
            {
                final HtmlParagraph popularityText = new HtmlParagraph(Context.tr("Popularity"), "offer_popularity_text");
                final HtmlParagraph popularityScore = new HtmlParagraph(HtmlTools.compressKarma(offer.getPopularity()), "offer_popularity_score");

                offerSummaryPopularity.add(popularityText);
                offerSummaryPopularity.add(popularityScore);

                if (!offer.isOwner()) {
                    final int vote = offer.getUserVoteValue();
                    if (vote == 0) {
                        final HtmlDiv offerPopularityJudge = new HtmlDiv("offer_popularity_judge");
                        {
                            // Useful
                            final PopularityVoteActionUrl usefulUrl = new PopularityVoteActionUrl(offer, true);
                            final HtmlLink usefulLink = usefulUrl.getHtmlLink("+");
                            usefulLink.setCssClass("useful");

                            // Useless
                            final PopularityVoteActionUrl uselessUrl = new PopularityVoteActionUrl(offer, false);
                            final HtmlLink uselessLink = uselessUrl.getHtmlLink("−");
                            uselessLink.setCssClass("useless");

                            offerPopularityJudge.add(usefulLink);
                            offerPopularityJudge.add(uselessLink);
                        }
                        offerSummaryPopularity.add(offerPopularityJudge);
                    } else {
                        // Already voted
                        final HtmlDiv offerPopularityJudged = new HtmlDiv("offer_popularity_judged");
                        {
                            if (vote > 0) {
                                offerPopularityJudged.add(new HtmlParagraph("+" + vote, "Useful"));
                            } else {
                                offerPopularityJudged.add(new HtmlParagraph("−" + Math.abs(vote), "useless"));
                            }
                        }
                        offerSummaryPopularity.add(offerPopularityJudged);
                    }
                } else {
                    final HtmlDiv offerPopularityNone = new HtmlDiv("offer_popularity_none");

                    offerSummaryPopularity.add(offerPopularityNone);
                }
            }
            return offerSummaryPopularity;
        }
    }
}
