package com.bloatit.web.utils.url;

import com.bloatit.web.utils.annotations.RequestParamSetter.Messages;

public abstract class UrlNode implements Iterable<UrlNode> {

    public UrlNode() {
        super();
    }

    public abstract UrlNode clone();

    public final String urlString() {
        StringBuilder sb = new StringBuilder();
        this.constructUrl(sb);
        return sb.toString();
    }

    public abstract Messages getMessages();

    protected abstract void parseParameters(final Parameters params, boolean pickValue);

    /**
     * Begin with a '/' and no slash at the end.
     */
    protected abstract void constructUrl(final StringBuilder sb);

    @Deprecated
    public abstract void addParameter(String name, String value);
}