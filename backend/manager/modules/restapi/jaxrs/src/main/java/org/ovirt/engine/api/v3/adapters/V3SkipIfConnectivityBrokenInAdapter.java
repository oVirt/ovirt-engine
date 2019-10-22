/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.SkipIfConnectivityBroken;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3SkipIfConnectivityBroken;

public class V3SkipIfConnectivityBrokenInAdapter implements V3Adapter<V3SkipIfConnectivityBroken, SkipIfConnectivityBroken> {
    @Override
    public SkipIfConnectivityBroken adapt(V3SkipIfConnectivityBroken from) {
        SkipIfConnectivityBroken to = new SkipIfConnectivityBroken();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        if (from.isSetThreshold()) {
            to.setThreshold(from.getThreshold());
        }
        return to;
    }
}
