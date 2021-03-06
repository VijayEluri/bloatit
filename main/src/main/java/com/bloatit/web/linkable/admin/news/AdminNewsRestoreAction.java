package com.bloatit.web.linkable.admin.news;

import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.RequestParam.Role;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.framework.webprocessor.url.Url;
import com.bloatit.model.Member;
import com.bloatit.model.NewsFeed;
import com.bloatit.model.right.UnauthorizedOperationException;
import com.bloatit.web.linkable.admin.master.AdminAction;
import com.bloatit.web.url.AdminNewsPageUrl;
import com.bloatit.web.url.AdminNewsRestoreActionUrl;

@ParamContainer("admin/dorestore")
public class AdminNewsRestoreAction extends AdminAction {

    @RequestParam(role = Role.GET)
    private final NewsFeed target;

    private final AdminNewsRestoreActionUrl url;

    public AdminNewsRestoreAction(final AdminNewsRestoreActionUrl url) {
        super(url);
        this.url = url;
        this.target = url.getTarget();
    }

    @Override
    protected Url doProcessAdmin() throws UnauthorizedOperationException {
        target.restore();
        session.notifyGood(Context.tr("Message ''{0}'' has been restored", target.getMessage()));
        return new AdminNewsPageUrl();
    }

    @Override
    protected Url checkRightsAndEverything(final Member me) {
        if (!target.isDeleted()) {
            session.notifyWarning("Canot restore a non deleted message.");
            return new AdminNewsPageUrl();
        }
        return NO_ERROR;
    }

    @Override
    protected Url doProcessErrors() {
        return new AdminNewsPageUrl();
    }

    @Override
    protected void transmitParameters() {
        session.addParameter(url.getTargetParameter());
    }
}
