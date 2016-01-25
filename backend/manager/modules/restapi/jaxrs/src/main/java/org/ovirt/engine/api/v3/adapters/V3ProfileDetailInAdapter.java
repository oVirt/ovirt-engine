/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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
