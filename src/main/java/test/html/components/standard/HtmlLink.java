/*
 * Copyright (C) 2010 BloatIt.
 * 
 * This file is part of BloatIt.
 * 
 * BloatIt is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * BloatIt is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with
 * BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */

package test.html.components.standard;

import test.html.HtmlElement;
import test.html.HtmlText;
import test.pages.HtmlContainerElement;

public class HtmlLink extends HtmlContainerElement {

    public HtmlLink(final String link, final HtmlElement element) {
        super("a");
        addAttribute("href", link);
        add(element);
    }

    public HtmlLink(final String link, final String element) {
        super("a");
        addAttribute("href", link);
        add(new HtmlText(element));
    }
}
