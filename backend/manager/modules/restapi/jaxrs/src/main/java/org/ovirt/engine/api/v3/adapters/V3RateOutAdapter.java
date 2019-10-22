/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Rate;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Rate;

public class V3RateOutAdapter implements V3Adapter<Rate, V3Rate> {
    @Override
    public V3Rate adapt(Rate from) {
        V3Rate to = new V3Rate();
        if (from.isSetBytes()) {
            to.setBytes(from.getBytes());
        }
        if (from.isSetPeriod()) {
            to.setPeriod(from.getPeriod());
        }
        return to;
    }
}
