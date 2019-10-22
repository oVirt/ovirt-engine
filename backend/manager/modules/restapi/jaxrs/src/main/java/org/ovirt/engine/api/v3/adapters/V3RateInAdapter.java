/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Rate;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Rate;

public class V3RateInAdapter implements V3Adapter<V3Rate, Rate> {
    @Override
    public Rate adapt(V3Rate from) {
        Rate to = new Rate();
        if (from.isSetBytes()) {
            to.setBytes(from.getBytes());
        }
        if (from.isSetPeriod()) {
            to.setPeriod(from.getPeriod());
        }
        return to;
    }
}
