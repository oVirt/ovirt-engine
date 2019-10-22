/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.NumaNodePin;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3NumaNodePin;

public class V3NumaNodePinOutAdapter implements V3Adapter<NumaNodePin, V3NumaNodePin> {
    @Override
    public V3NumaNodePin adapt(NumaNodePin from) {
        V3NumaNodePin to = new V3NumaNodePin();
        if (from.isSetHostNumaNode()) {
            to.setHostNumaNode(adaptOut(from.getHostNumaNode()));
        }
        if (from.isSetIndex()) {
            to.setIndex(from.getIndex());
        }
        if (from.isSetPinned()) {
            to.setPinned(from.isPinned());
        }
        return to;
    }
}
