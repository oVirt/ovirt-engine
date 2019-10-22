/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Range;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Range;

public class V3RangeOutAdapter implements V3Adapter<Range, V3Range> {
    @Override
    public V3Range adapt(Range from) {
        V3Range to = new V3Range();
        if (from.isSetFrom()) {
            to.setFrom(from.getFrom());
        }
        if (from.isSetTo()) {
            to.setTo(from.getTo());
        }
        return to;
    }
}
