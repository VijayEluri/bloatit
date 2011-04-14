package com.bloatit.framework.webprocessor.components.advanced.showdown;

import org.apache.commons.lang.RandomStringUtils;

import com.bloatit.framework.webprocessor.components.HtmlDiv;
import com.bloatit.framework.webprocessor.components.HtmlGenericElement;
import com.bloatit.framework.webprocessor.components.meta.HtmlLeaf;
import com.bloatit.framework.webprocessor.components.meta.XmlText;

public class MarkdownPreviewer extends HtmlLeaf {
    private final HtmlDiv output;

    public MarkdownPreviewer(final MarkdownEditor source) {
        this.output = new HtmlDiv("md_preview");
        add(output);
        final String id = "blmdprev-" + RandomStringUtils.randomAlphabetic(10);
        output.setId(id);
        final HtmlGenericElement script = new HtmlGenericElement("script");

        script.add(new XmlText("setup_wmd({ input: \"" + source.getInputId() + "\", button_bar: \"" + source.getButtonBarId() + "\", preview: \""
                + output.getId() + "\", output: \"copy_html\" });"));
        add(script);
    }
}