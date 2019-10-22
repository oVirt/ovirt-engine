/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.BlockStatistic;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3BlockStatistic;

public class V3BlockStatisticOutAdapter implements V3Adapter<BlockStatistic, V3BlockStatistic> {
    @Override
    public V3BlockStatistic adapt(BlockStatistic from) {
        V3BlockStatistic to = new V3BlockStatistic();
        if (from.isSetStatistics()) {
            to.getStatistic().addAll(adaptOut(from.getStatistics().getStatistics()));
        }
        return to;
    }
}
