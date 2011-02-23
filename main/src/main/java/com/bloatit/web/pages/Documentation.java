package com.bloatit.web.pages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bloatit.common.Log;
import com.bloatit.framework.exceptions.FatalErrorException;
import com.bloatit.framework.exceptions.RedirectException;
import com.bloatit.framework.webserver.Context;
import com.bloatit.framework.webserver.annotations.Optional;
import com.bloatit.framework.webserver.annotations.ParamContainer;
import com.bloatit.framework.webserver.annotations.RequestParam;
import com.bloatit.framework.webserver.components.HtmlDiv;
import com.bloatit.framework.webserver.components.renderer.HtmlMarkdownRenderer;
import com.bloatit.web.WebConfiguration;
import com.bloatit.web.pages.master.MasterPage;
import com.bloatit.web.url.DocumentationUrl;

/**
 * <p>
 * A holding class for documentation
 * </p>
 * <p>
 * Documentation system is based on markdown files hosted on the server. This
 * page is a container used to view these markdown documents. <br />
 * Document to display is chosen via the GET parameter Documenvalue
 * tation#DOC_TARGET.
 * </p>
 */
@ParamContainer("documentation")
public class Documentation extends MasterPage {
    private final static String DOC_TARGET = "doc";
    private final static String DEFAULT_DOC = "home";

    /**
     * <p>
     * Store the html documents (after they've been converted from markdown)
     * </p>
     */
    private static Map<MarkdownDocumentationMarker, MarkdownDocumentationContent> cache = Collections.synchronizedMap((new HashMap<MarkdownDocumentationMarker, MarkdownDocumentationContent>()));

    @RequestParam(name = DOC_TARGET)
    @Optional(DEFAULT_DOC)
    private final String docTarget;

    public Documentation(final DocumentationUrl url) {
        super(url);
        docTarget = url.getDocTarget();
    }

    @Override
    public boolean isStable() {
        return true;
    }

    @Override
    protected void doCreate() throws RedirectException {
        final String dir = WebConfiguration.getBloatitDocumentationDir();
        final HtmlDiv master = new HtmlDiv("padding_box");
        add(master);

        FileInputStream fis;
        final String language = Context.getLocalizator().getLanguageCode();
        try {
            File targetFile = new File(dir + "/" + docTarget + "_" + language);

            if (!targetFile.exists()) {
                Log.web().warn("User tried to access doc file " + docTarget + "_" + language + " but it doesn't exist.");
                session.notifyBad(Context.tr("Documentation file {0} doesn''t exist in language {1}, using english instead", docTarget, language));
                targetFile = new File(dir + docTarget + "_" + "en");
            }
            fis = new FileInputStream(targetFile);

            final MarkdownDocumentationMarker mdm = new MarkdownDocumentationMarker(docTarget, language);
            final MarkdownDocumentationContent mdc = cache.get(mdm);

            if (mdc == null || mdc.savedDate.before(new Date(targetFile.lastModified()))) {
                // No content, or content has been saved before the file was
                // last modified
                Log.web().trace("Reading from the markdown documentation file " + docTarget);
                final byte[] b = new byte[fis.available()];
                fis.read(b);
                final String markDownContent = new String(b);
                final HtmlMarkdownRenderer content = new HtmlMarkdownRenderer(markDownContent);
                cache.put(mdm, new MarkdownDocumentationContent(new Date(), content.getRendereredContent()));
                master.add(content);
            } else {
                Log.web().trace("Using cache for documentation file " + docTarget);
                master.add(new HtmlMarkdownRenderer(mdc.htmlString, true));
            }

        } catch (final FileNotFoundException e) {
            // User asked a wrong documentation file, redirecting him to the doc
            // home
            Log.web().warn("A user tries to access documentation file " + docTarget + " but file is not available.");
            session.notifyBad(Context.tr("Documentation entry {0} doesn''t exist. Sending you to documentation home page", docTarget));

            final DocumentationUrl redirectTo = new DocumentationUrl();
            redirectTo.setDocTarget(DEFAULT_DOC);
            throw new RedirectException(redirectTo);
        } catch (final IOException e) {
            throw new FatalErrorException("An error occured while parsing the documentation file " + docTarget, e);
        }
    }

    @Override
    protected String getPageTitle() {
        return Context.tr("Elveos documentation: {0}", docTarget);
    }

    /**
     * Nested class used as a key to cache parsed content
     */
    private class MarkdownDocumentationMarker {
        public String name;
        public String lang;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((lang == null) ? 0 : lang.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MarkdownDocumentationMarker other = (MarkdownDocumentationMarker) obj;
            if (lang == null) {
                if (other.lang != null) {
                    return false;
                }
            } else if (!lang.equals(other.lang)) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            return true;
        }

        public MarkdownDocumentationMarker(final String name, final String lang) {
            super();
            this.name = name;
            this.lang = lang;
        }
    }

    /**
     * Nested class used as a MapEntry.value to cache parsed markdown content
     */
    private class MarkdownDocumentationContent {
        public Date savedDate;
        public String htmlString;

        public MarkdownDocumentationContent(final Date savedDate, final String htmlString) {
            super();
            this.savedDate = savedDate;
            this.htmlString = htmlString;
        }
    }
}
