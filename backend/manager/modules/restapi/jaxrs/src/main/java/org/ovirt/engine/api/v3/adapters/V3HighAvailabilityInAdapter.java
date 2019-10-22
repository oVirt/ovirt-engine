/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.HighAvailability;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3HighAvailability;

public class V3HighAvailabilityInAdapter implements V3Adapter<V3HighAvailability, HighAvailability> {
    @Override
    public HighAvailability adapt(V3HighAvailability from) {
        HighAvailability to = new HighAvailability();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        if (from.isSetPriority()) {
            to.setPriority(from.getPriority());
        }
        return to;
    }
}
