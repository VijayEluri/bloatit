package com.bloatit.web.linkable.team;

import org.apache.commons.lang.NotImplementedException;

import com.bloatit.framework.exceptions.highlevel.ShallNotPassException;
import com.bloatit.framework.exceptions.lowlevel.UnauthorizedOperationException;
import com.bloatit.framework.webprocessor.annotations.ParamContainer;
import com.bloatit.framework.webprocessor.annotations.RequestParam;
import com.bloatit.framework.webprocessor.annotations.RequestParam.Role;
import com.bloatit.framework.webprocessor.context.Context;
import com.bloatit.framework.webprocessor.url.Url;
import com.bloatit.model.Member;
import com.bloatit.model.Team;
import com.bloatit.web.actions.LoggedAction;
import com.bloatit.web.url.SendTeamInvitationActionUrl;

/**
 * <p>
 * An action used to send team invitations
 * </p>
 */
@ParamContainer("invitation/dosend")
public class SendTeamInvitationAction extends LoggedAction {
    public final static String TEAM_JOIN_CODE = "bloatit_join_team";
    public final static String RECEIVER_CODE = "bloatit_join_receiver";

    @RequestParam(name = TEAM_JOIN_CODE, role = Role.POST)
    private final Team team;

    @RequestParam(name = RECEIVER_CODE, role = Role.POST)
    private final Member receiver;

    // keep it for consistency
    @SuppressWarnings("unused")
    private final SendTeamInvitationActionUrl url;

    public SendTeamInvitationAction(final SendTeamInvitationActionUrl url) {
        super(url);
        this.url = url;
        this.team = url.getTeam();
        this.receiver = url.getReceiver();
    }

    @Override
    public Url doProcessRestricted(final Member me) {
        if (!me.canSendInvitation(team)) {
            session.notifyBad(Context.tr("You are not allowed to send invitations for this team."));
            return session.getLastVisitedPage();
        }

        try {
            me.sendInvitation(receiver, team);
            session.notifyGood(Context.tr("Invitation sent to {0} for team {0}.", receiver.getDisplayName(), team.getLogin()));
        } catch (final UnauthorizedOperationException e) {
            session.notifyBad(Context.tr("Oops, an error prevented us from sendint this invitaton. Please notify us of the bug."));
            throw new ShallNotPassException("User couldn't send a team invitation, while he should be able to", e);
        }
        return session.getLastVisitedPage();
    }

    @Override
    protected Url doCheckRightsAndEverything(final Member authenticatedMember) {
        return NO_ERROR;
    }

    @Override
    protected Url doProcessErrors() {
        return session.getLastVisitedPage();
    }

    @Override
    protected String getRefusalReason() {
        return Context.tr("You must be logged to send an invitation");
    }

    @Override
    protected void transmitParameters() {
        throw new NotImplementedException();
    }
}
