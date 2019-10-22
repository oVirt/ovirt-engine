/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.BlockStatistics;
import org.ovirt.engine.api.model.FopStatistics;
import org.ovirt.engine.api.model.ProfileDetail;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ProfileDetail;

public class V3ProfileDetailInAdapter implements V3Adapter<V3ProfileDetail, ProfileDetail> {
    @Override
    public ProfileDetail adapt(V3ProfileDetail from) {
        ProfileDetail to = new ProfileDetail();
        if (from.isSetBlockStatistic()) {
            to.setBlockStatistics(new BlockStatistics());
            to.getBlockStatistics().getBlockStatistics().addAll(adaptIn(from.getBlockStatistic()));
        }
        if (from.isSetDuration()) {
            to.setDuration(from.getDuration());
        }
        if (from.isSetFopStatistic()) {
            to.setFopStatistics(new FopStatistics());
            to.getFopStatistics().getFopStatistics().addAll(adaptIn(from.getFopStatistic()));
        }
        if (from.isSetProfileType()) {
            to.setProfileType(from.getProfileType());
        }
        if (from.isSetStatistic()) {
            to.setStatistics(new Statistics());
            to.getStatistics().getStatistics().addAll(adaptIn(from.getStatistic()));
        }
        return to;
    }
}
