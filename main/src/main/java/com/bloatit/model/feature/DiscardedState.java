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
package com.bloatit.model.feature;

import com.bloatit.data.DaoFeature.FeatureState;
import com.bloatit.model.FeatureImplementation;

/**
 * The Class DiscardedState.
 */
public class DiscardedState extends AbstractFeatureState {

    /**
     * Instantiates a new discarded state.
     * 
     * @param feature the feature on which this state apply.
     */
    public DiscardedState(final FeatureImplementation feature) {
        super(feature);
        feature.setFeatureStateUnprotected(getState());
    }

    /*
     * (non-Javadoc)
     * @see
     * com.bloatit.model.feature.AbstractFeatureState#eventPopularityPending()
     */
    @Override
    public AbstractFeatureState eventPopularityPending() {
        return new PendingState(feature);
    }

    /*
     * (non-Javadoc)
     * @see com.bloatit.model.feature.AbstractFeatureState#popularityValidated()
     */
    @Override
    public AbstractFeatureState popularityValidated() {
        return new PendingState(feature);
    }

    @Override
    public final FeatureState getState() {
        return FeatureState.DISCARDED;
    }

}
