//
// Copyright (c) 2011 Linkeos.
//
// This file is part of Elveos.org.
// Elveos.org is free software: you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation, either version 3 of the License, or (at your
// option) any later version.
//
// Elveos.org is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
// more details.
// You should have received a copy of the GNU General Public License along
// with Elveos.org. If not, see http://www.gnu.org/licenses/.
//
package com.bloatit.web.linkable.money;

import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.ParamContainer.Protocol;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.RequestParam.Role;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.framework.webprocessor.url.Url;
import com.bloatit.model.Member;
import com.bloatit.web.linkable.master.LoggedElveosAction;
import com.bloatit.web.url.AccountChargingPageUrl;
import com.bloatit.web.url.UnlockAccountChargingProcessActionUrl;

@ParamContainer(value = "account/charging/process/%process%/unlock", protocol = Protocol.HTTPS)
public final class UnlockAccountChargingProcessAction extends LoggedElveosAction {

    @RequestParam(role = Role.PAGENAME)
    private final AccountChargingProcess process;

    public UnlockAccountChargingProcessAction(final UnlockAccountChargingProcessActionUrl url) {
        super(url);
        process = url.getProcess();
    }

    @Override
    protected Url checkRightsAndEverything(final Member me) {
        return NO_ERROR;
    }

    @Override
    protected Url doProcessRestricted(final Member me) {
        process.setLock(false);
        return new AccountChargingPageUrl(process);
    }

    @Override
    protected Url doProcessErrors() {
        return session.pickPreferredPage();
    }

    @Override
    protected String getRefusalReason() {
        return Context.tr("You cannot modify a contribution without being logged.");
    }

    @Override
    protected void transmitParameters() {
        // nothing to do
    }

}
