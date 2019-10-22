/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Range;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Range;

public class V3RangeInAdapter implements V3Adapter<V3Range, Range> {
    @Override
    public Range adapt(V3Range from) {
        Range to = new Range();
        if (from.isSetFrom()) {
            to.setFrom(from.getFrom());
        }
        if (from.isSetTo()) {
            to.setTo(from.getTo());
        }
        return to;
    }
}
