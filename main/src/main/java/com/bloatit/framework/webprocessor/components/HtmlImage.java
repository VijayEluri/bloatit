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

package com.bloatit.framework.webprocessor.components;

import com.bloatit.framework.model.Image;
import com.bloatit.framework.webprocessor.components.meta.HtmlLeaf;
import com.bloatit.framework.webprocessor.url.Url;

/**
 * Used to display an image
 */
public class HtmlImage extends HtmlLeaf {
    public HtmlImage(final Image image, final String alt) {
        super("img");
        String uri = "";
        uri = image.getIdentifier();
        addAttribute("src", uri);
        if(alt != null) {
            addAttribute("alt", alt);
        }
    }

    public HtmlImage(final Image image, final String alt, final String cssClass) {
        this(image, alt);
        addAttribute("class", cssClass);
    }

    private HtmlImage(final Url imageUrl, final String alt) {
        super("img");
        addAttribute("src", imageUrl.urlString());
        if(alt != null) {
            addAttribute("alt", alt);
        }
    }

    public HtmlImage(final Url imageUrl, final String alt, final String cssClass) {
        this(imageUrl, alt);
        addAttribute("class", cssClass);
    }

    public HtmlImage(String url, String alt, String cssClass) {
        super("img");
        addAttribute("src", url);
        if(alt != null) {
            addAttribute("alt", alt);
        }
        addAttribute("class", cssClass);
    }
    
    public HtmlImage(String url) {
        super("img");
        addAttribute("src", url);
    }
}
