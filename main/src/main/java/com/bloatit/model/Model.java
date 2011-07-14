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
package com.bloatit.model;

import javassist.NotFoundException;

import com.bloatit.common.Log;
import com.bloatit.data.DataManager;
import com.bloatit.data.queries.DBRequests;
import com.bloatit.framework.utils.PageIterable;
import com.bloatit.framework.xcgiserver.RequestKey;
import com.bloatit.model.feature.FeatureList;
import com.bloatit.model.feature.TaskUpdateDevelopingState;
import com.bloatit.model.right.AuthToken;

public class Model implements com.bloatit.framework.model.Model {
    public Model() {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.AbstractModelManager#launch()
     */
    @Override
    public void initialize() {
        DataManager.initialize();
        Log.model().trace("Launching the Model.");
        ModelConfiguration.loadConfiguration();

        open();
        // Find the feature with selected offer that should pass into validated.
        final PageIterable<Feature> featuresToValidate = new FeatureList(DBRequests.featuresThatShouldBeValidated());
        for (final Feature feature : featuresToValidate) {
            feature.updateDevelopmentState();
        }

        // Find the feature with selected offer that should pass into validated.
        final PageIterable<Feature> featuresToValidateInTheFuture = new FeatureList(DBRequests.featuresThatShouldBeValidatedInTheFuture());
        for (final Feature feature : featuresToValidateInTheFuture) {
            new TaskUpdateDevelopingState(feature.getId(), feature.getValidationDate());
        }
        close();
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.AbstractModelManager#shutdown()
     */
    @Override
    public void shutdown() {
        Log.model().trace("Shutdowning the Model.");
        DataManager.shutdown();
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.AbstractModelManager#openReadOnly()
     */
    @Override
    public void setReadOnly() {
        Log.model().trace("This transaction is Read Only.");
        DataManager.setReadOnly();
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.AbstractModelManager#open()
     */
    @Override
    public void open() {
        Log.model().trace("Open a new transaction.");
        AuthToken.unAuthenticate();
        DataManager.open();
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.AbstractModelManager#close()
     */
    @Override
    public void close() {
        Log.model().trace("Close the current transaction.");
        CacheManager.clear();
        DataManager.close();
    }

    @Override
    public void rollback() {
        CacheManager.clear();
        DataManager.rollback();
    }

    @Override
    public void authenticate(final RequestKey key) {
        try {
            switch (key.getSource()) {
                case COOKIE:
                    AuthToken.authenticate(key);
                    break;
                case TOKEN:
                    AuthToken.authenticate(key.getId());
                    break;
                case GENERATED:
                    // No authentication on just generated key ...
                    break;
                default:
                    break;
            }
        } catch (final NotFoundException e) {
            Log.model().trace("authentication error.", e);
        }
    }
}
