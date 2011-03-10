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
package com.bloatit.web.linkable.softwares;

import static com.bloatit.framework.webserver.Context.tr;

import java.util.Locale;

import com.bloatit.framework.exceptions.RedirectException;
import com.bloatit.framework.exceptions.UnauthorizedOperationException;
import com.bloatit.framework.webserver.Context;
import com.bloatit.framework.webserver.PageNotFoundException;
import com.bloatit.framework.webserver.annotations.ParamConstraint;
import com.bloatit.framework.webserver.annotations.ParamContainer;
import com.bloatit.framework.webserver.annotations.RequestParam;
import com.bloatit.framework.webserver.annotations.tr;
import com.bloatit.framework.webserver.components.HtmlDiv;
import com.bloatit.framework.webserver.components.HtmlImage;
import com.bloatit.framework.webserver.components.HtmlParagraph;
import com.bloatit.framework.webserver.components.HtmlTitle;
import com.bloatit.framework.webserver.components.renderer.HtmlRawTextRenderer;
import com.bloatit.model.FileMetadata;
import com.bloatit.model.Software;
import com.bloatit.model.Translation;
import com.bloatit.web.pages.master.MasterPage;
import com.bloatit.web.url.FileResourceUrl;
import com.bloatit.web.url.SoftwarePageUrl;

@ParamContainer("software")
public final class SoftwarePage extends MasterPage {

    public static final String SOFTWARE_FIELD_NAME = "id";

    @ParamConstraint(optionalErrorMsg = @tr("The id of the software is incorrect or missing"))
    @RequestParam(name = SOFTWARE_FIELD_NAME)
    private final Software software;

    private final SoftwarePageUrl url;

    public SoftwarePage(final SoftwarePageUrl url) {
        super(url);
        this.url = url;
        this.software = url.getSoftware();
    }

    @Override
    protected void doCreate() throws RedirectException {
        session.notifyList(url.getMessages());
        if (url.getMessages().hasMessage()) {
            throw new PageNotFoundException();
        }

        try {

            final HtmlDiv box = new HtmlDiv("padding_box");

            HtmlTitle softwareName;
            softwareName = new HtmlTitle(software.getName(), 1);
            box.add(softwareName);

            FileMetadata image = software.getImage();
            if (image != null) {
                box.add(new HtmlImage(new FileResourceUrl(image), image.getShortDescription(), "float_right"));
            }

            final Locale defaultLocale = Context.getLocalizator().getLocale();
            final Translation translatedDescription = software.getDescription().getTranslationOrDefault(defaultLocale);

            final HtmlParagraph shortDescription = new HtmlParagraph(new HtmlRawTextRenderer(translatedDescription.getTitle()));
            final HtmlParagraph description = new HtmlParagraph(new HtmlRawTextRenderer(translatedDescription.getText()));

            box.add(shortDescription);
            box.add(description);

            add(box);
        } catch (final UnauthorizedOperationException e) {
            add(new HtmlParagraph(tr("For obscure reasons, you are not allowed to see the details of this software.")));
        }
    }

    @Override
    protected String getPageTitle() {
        if (software != null) {
            try {
                return tr("Software - ") + software.getName();
            } catch (final UnauthorizedOperationException e) {
                return tr("Software - Windows 8");
            }
        }
        return tr("Member - No member");
    }

    @Override
    public boolean isStable() {
        return true;
    }
}