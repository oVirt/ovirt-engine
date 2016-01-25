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

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.ProfileDetail;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ProfileDetail;

public class V3ProfileDetailOutAdapter implements V3Adapter<ProfileDetail, V3ProfileDetail> {
    @Override
    public V3ProfileDetail adapt(ProfileDetail from) {
        V3ProfileDetail to = new V3ProfileDetail();
        if (from.isSetBlockStatistics()) {
            to.getBlockStatistic().addAll(adaptOut(from.getBlockStatistics().getBlockStatistics()));
        }
        if (from.isSetDuration()) {
            to.setDuration(from.getDuration());
        }
        if (from.isSetFopStatistics()) {
            to.getFopStatistic().addAll(adaptOut(from.getFopStatistics().getFopStatistics()));
        }
        if (from.isSetProfileType()) {
            to.setProfileType(from.getProfileType());
        }
        if (from.isSetStatistics()) {
            to.getStatistic().addAll(adaptOut(from.getStatistics().getStatistics()));
        }
        return to;
    }
}
