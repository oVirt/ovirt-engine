/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.BlockStatistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3BlockStatistic;

public class V3BlockStatisticInAdapter implements V3Adapter<V3BlockStatistic, BlockStatistic> {
    public BlockStatistic adapt(V3BlockStatistic from) {
        BlockStatistic to = new BlockStatistic();
        if (from.isSetStatistic()) {
            to.setStatistics(new Statistics());
            to.getStatistics().getStatistics().addAll(adaptIn(from.getStatistic()));
        }
        return to;
    }
}
