/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.TimeZone;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3TimeZone;

public class V3TimeZoneOutAdapter implements V3Adapter<TimeZone, V3TimeZone> {
    @Override
    public V3TimeZone adapt(TimeZone from) {
        V3TimeZone to = new V3TimeZone();
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetUtcOffset()) {
            to.setUtcOffset(from.getUtcOffset());
        }
        return to;
    }
}
