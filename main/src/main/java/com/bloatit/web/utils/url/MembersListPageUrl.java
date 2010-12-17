package com.bloatit.web.utils.url;

import com.bloatit.web.annotations.Message.Level;
import com.bloatit.web.annotations.RequestParam.Role;
import com.bloatit.web.utils.url.Parameter;
import com.bloatit.web.utils.annotations.Loaders;
import com.bloatit.web.utils.annotations.RequestParamSetter.ConversionErrorException;
import com.bloatit.web.exceptions.RedirectException;

@SuppressWarnings("unused")
public class MembersListPageUrl extends Url {
public static String getName() { return "MembersListPage"; }
public com.bloatit.web.html.pages.MembersListPage createPage() throws RedirectException{ 
    return new com.bloatit.web.html.pages.MembersListPage(this); }
public MembersListPageUrl(Parameters params) {
    super(getName());
    parseParameters(params);
}
public MembersListPageUrl() {
    super(getName());
}
private HtmlPagedListUrl pagedMemberListUrl = new HtmlPagedListUrl();

public HtmlPagedListUrl getPagedMemberListUrl(){ 
    return this.pagedMemberListUrl;
}

public void setPagedMemberListUrl(HtmlPagedListUrl arg){ 
    this.pagedMemberListUrl = arg;
}


@Override 
protected void doRegister() { 
    register(pagedMemberListUrl);
}

public MembersListPageUrl clone() { 
    MembersListPageUrl other = new MembersListPageUrl();
    other.pagedMemberListUrl = this.pagedMemberListUrl.clone();
    return other;
}
}