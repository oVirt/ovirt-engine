/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.FopStatistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3FopStatistic;

public class V3FopStatisticInAdapter implements V3Adapter<V3FopStatistic, FopStatistic> {
    public FopStatistic adapt(V3FopStatistic from) {
        FopStatistic to = new FopStatistic();
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetStatistic()) {
            to.setStatistics(new Statistics());
            to.getStatistics().getStatistics().addAll(adaptIn(from.getStatistic()));
        }
        return to;
    }
}
