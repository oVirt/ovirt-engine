/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.TimeZone;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3TimeZone;

public class V3TimeZoneInAdapter implements V3Adapter<V3TimeZone, TimeZone> {
    @Override
    public TimeZone adapt(V3TimeZone from) {
        TimeZone to = new TimeZone();
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetUtcOffset()) {
            to.setUtcOffset(from.getUtcOffset());
        }
        return to;
    }
}
