/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.NumaNodePin;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3NumaNodePin;

public class V3NumaNodePinInAdapter implements V3Adapter<V3NumaNodePin, NumaNodePin> {
    @Override
    public NumaNodePin adapt(V3NumaNodePin from) {
        NumaNodePin to = new NumaNodePin();
        if (from.isSetHostNumaNode()) {
            to.setHostNumaNode(adaptIn(from.getHostNumaNode()));
        }
        if (from.isSetIndex()) {
            to.setIndex(from.getIndex());
        }
        to.setPinned(from.isPinned());
        return to;
    }
}
