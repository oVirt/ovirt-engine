/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Value;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Value;

public class V3ValueInAdapter implements V3Adapter<V3Value, Value> {
    @Override
    public Value adapt(V3Value from) {
        Value to = new Value();
        if (from.isSetDatum()) {
            to.setDatum(from.getDatum());
        }
        if (from.isSetDetail()) {
            to.setDetail(from.getDetail());
        }
        return to;
    }
}
