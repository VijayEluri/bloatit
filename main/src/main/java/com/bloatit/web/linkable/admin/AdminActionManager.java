package com.bloatit.web.linkable.admin;

import java.util.EnumSet;

import com.bloatit.framework.webprocessor.components.form.Displayable;
import com.bloatit.framework.webprocessor.context.Context;

public class AdminActionManager {

    public enum Action implements Displayable {

        DELETE(tr("delete")), //
        RESTORE(tr("restore")), //

        LOCK(tr("lock votes")), //
        UNLOCK(tr("unlock votes")), //
        SETSTATE(tr("Change the state")), //

        UPDATE_DEVELOPMENT_STATE(tr("Re calculate if this feature should passe into development.")), //
        COMPUTE_SELECTED_OFFER(tr("Re calculate the selected offer")), //
        SET_VALIDATION_DATE(tr("Update the validation Date")), //
        SET_FEATURE_IN_DEVELOPMENT(tr("Change the feature state")), //

        VALIDATE_BATCH(tr("Validate milestone if possible")), //
        FORCE_VALIDATE_BATCH(tr("Validate milestone --force !")), //
        ;
        private final String displayName;

        Action(final String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String getDisplayName() {
            return Context.tr(displayName);
        }

        //Fake tr
        private static String tr(String fake) {
            return fake;
        }
    }

    protected EnumSet<Action> userContentActions() {
        return EnumSet.range(Action.DELETE, Action.RESTORE);
    }

    protected EnumSet<Action> kudosableActions() {
        return EnumSet.range(Action.LOCK, Action.SETSTATE);
    }

    protected EnumSet<Action> featureActions() {
        return EnumSet.range(Action.UPDATE_DEVELOPMENT_STATE, Action.SET_FEATURE_IN_DEVELOPMENT);
    }

    protected EnumSet<Action> milestoneActions() {
        return EnumSet.range(Action.VALIDATE_BATCH, Action.FORCE_VALIDATE_BATCH);
    }
}
