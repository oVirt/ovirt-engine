/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.FopStatistic;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3FopStatistic;

public class V3FopStatisticOutAdapter implements V3Adapter<FopStatistic, V3FopStatistic> {
    @Override
    public V3FopStatistic adapt(FopStatistic from) {
        V3FopStatistic to = new V3FopStatistic();
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetStatistics()) {
            to.getStatistic().addAll(adaptOut(from.getStatistics().getStatistics()));
        }
        return to;
    }
}
