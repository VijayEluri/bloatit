/*
 * Copyright (C) 2010 BloatIt. This file is part of BloatIt. BloatIt is free software: you
 * can redistribute it and/or modify it under the terms of the GNU Affero General Public
 * License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version. BloatIt is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details. You should have received a copy of the GNU Affero General
 * Public License along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */

package com.bloatit.web.html.components.custom;

import java.text.NumberFormat;
import java.util.Locale;

import com.bloatit.common.Image;
import com.bloatit.framework.Demand;
import com.bloatit.framework.Translation;
import com.bloatit.web.html.HtmlTagText;
import com.bloatit.web.html.components.standard.HtmlDiv;
import com.bloatit.web.html.components.standard.HtmlGenericElement;
import com.bloatit.web.html.components.standard.HtmlImage;
import com.bloatit.web.html.components.standard.HtmlLink;
import com.bloatit.web.html.components.standard.HtmlParagraph;
import com.bloatit.web.html.components.standard.HtmlTitleBlock;
import com.bloatit.web.server.Context;
import com.bloatit.web.utils.i18n.CurrencyLocale;
import com.bloatit.web.utils.url.IdeaPageUrl;
import com.bloatit.web.utils.url.OfferPageUrl;

public class HtmlIdeaSumary extends HtmlDiv {

	public HtmlIdeaSumary(final Demand idea) {
		super("idea_conpact_summary");

		final HtmlDiv leftBlock = new HtmlDiv("idea_summary_left");
		{

			final HtmlDiv karmaBlock = new HtmlDiv("idea_karma");
			karmaBlock.add(new HtmlParagraph("" + idea.getPopularity()));

			leftBlock.add(karmaBlock);

		}
		add(leftBlock);

		final HtmlDiv centerBlock = new HtmlDiv("idea_summary_center");
		{

			HtmlGenericElement project = new HtmlGenericElement("span");
			project.setCssClass("project");
			project.addText("VLC");

			final HtmlLink linkTitle = new IdeaPageUrl(idea).getHtmlLink("");
			linkTitle.setCssClass("idea_link");

			linkTitle.add(project);
			linkTitle.addText(" - ");
			linkTitle.addText(idea.getTitle());

			final HtmlTitleBlock ideaTitle = new HtmlTitleBlock(linkTitle, 3);
			{

				final Locale defaultLocale = Context.getLocalizator().getLocale();
				final Translation translatedDescription = idea.getDescription().getTranslationOrDefault(defaultLocale);
				String shortDescription = translatedDescription.getText();

				if (shortDescription.length() > 144) {
					// TODO create a tools to truncate less dirty
					shortDescription = shortDescription.substring(0, 143) + " ...";
				}

				final HtmlLink linkText = new IdeaPageUrl(idea).getHtmlLink(new HtmlParagraph(shortDescription));
				linkText.setCssClass("idea_link_text");

				ideaTitle.add(linkText);

				float progressValue = (float) Math.floor(idea.getProgression());
				float cappedProgressValue = progressValue;
				if (cappedProgressValue > 100) {
					cappedProgressValue = 100;
				}

				final HtmlProgressBar progressBar = new HtmlProgressBar(cappedProgressValue);
				ideaTitle.add(progressBar);

				if (idea.getCurrentOffer() == null) {

					HtmlGenericElement amount = new HtmlGenericElement("span");
					amount.setCssClass("important");

					CurrencyLocale currency = Context.getLocalizator().getCurrency(idea.getContribution());

					amount.addText(currency.getDefaultString());

					final HtmlParagraph progressText = new HtmlParagraph();
					progressText.setCssClass("idea_progress_text");

					progressText.add(amount);
					progressText.addText(Context.tr(" no offer ("));
					progressText.add(new OfferPageUrl(idea).getHtmlLink(Context.tr("make an offer")));
					progressText.addText(Context.tr(")"));

					ideaTitle.add(progressText);
				} else {
					// Amount
					CurrencyLocale amountCurrency = Context.getLocalizator().getCurrency(idea.getContribution());
					HtmlGenericElement amount = new HtmlGenericElement("span");
					amount.setCssClass("important");
					amount.addText(amountCurrency.getDefaultString());

					// Target
					CurrencyLocale targetCurrency = Context.getLocalizator().getCurrency(idea.getCurrentOffer().getAmount());
					HtmlGenericElement target = new HtmlGenericElement("span");
					target.setCssClass("important");
					target.addText(targetCurrency.getDefaultString());

					// Progress
					HtmlGenericElement progress = new HtmlGenericElement("span");
					progress.setCssClass("important");
					NumberFormat format = NumberFormat.getNumberInstance();
					format.setMinimumFractionDigits(0);
					progress.addText("" + format.format(progressValue) + " %");

					final HtmlParagraph progressText = new HtmlParagraph();
					progressText.setCssClass("idea_progress_text");

					progressText.add(amount);
					progressText.addText(Context.tr(" i.e. "));
					progressText.add(progress);
					progressText.addText(Context.tr(" of "));
					progressText.add(target);
					long amountLong = idea.getCurrentOffer().getAmount().longValue();
					progressText.addText(Context.trn(" requested ", " requested ", amountLong));

					ideaTitle.add(progressText);
				}

			}
			centerBlock.add(ideaTitle);

		}
		// ideaLinkBlock.add(centerBlock);
		add(centerBlock);

		final HtmlDiv rightBlock = new HtmlDiv("idea_summary_right");
		{
			rightBlock.add(new HtmlImage(new Image("/resources/img/idea.png", Image.ImageType.DISTANT)));
		}
		// ideaLinkBlock.add(rightBlock);
		add(rightBlock);
	}

}