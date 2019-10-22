/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.SkipIfConnectivityBroken;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3SkipIfConnectivityBroken;

public class V3SkipIfConnectivityBrokenOutAdapter implements V3Adapter<SkipIfConnectivityBroken, V3SkipIfConnectivityBroken> {
    @Override
    public V3SkipIfConnectivityBroken adapt(SkipIfConnectivityBroken from) {
        V3SkipIfConnectivityBroken to = new V3SkipIfConnectivityBroken();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        if (from.isSetThreshold()) {
            to.setThreshold(from.getThreshold());
        }
        return to;
    }
}
