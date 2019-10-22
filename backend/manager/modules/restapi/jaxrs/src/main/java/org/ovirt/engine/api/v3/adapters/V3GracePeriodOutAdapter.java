/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.GracePeriod;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GracePeriod;

public class V3GracePeriodOutAdapter implements V3Adapter<GracePeriod, V3GracePeriod> {
    @Override
    public V3GracePeriod adapt(GracePeriod from) {
        V3GracePeriod to = new V3GracePeriod();
        if (from.isSetExpiry()) {
            to.setExpiry(from.getExpiry());
        }
        return to;
    }
}
