/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.HighAvailability;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3HighAvailability;

public class V3HighAvailabilityOutAdapter implements V3Adapter<HighAvailability, V3HighAvailability> {
    @Override
    public V3HighAvailability adapt(HighAvailability from) {
        V3HighAvailability to = new V3HighAvailability();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        if (from.isSetPriority()) {
            to.setPriority(from.getPriority());
        }
        return to;
    }
}
