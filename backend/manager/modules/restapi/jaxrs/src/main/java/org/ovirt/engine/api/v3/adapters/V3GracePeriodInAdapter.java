/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.GracePeriod;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GracePeriod;

public class V3GracePeriodInAdapter implements V3Adapter<V3GracePeriod, GracePeriod> {
    @Override
    public GracePeriod adapt(V3GracePeriod from) {
        GracePeriod to = new GracePeriod();
        if (from.isSetExpiry()) {
            to.setExpiry((int) from.getExpiry());
        }
        return to;
    }
}
