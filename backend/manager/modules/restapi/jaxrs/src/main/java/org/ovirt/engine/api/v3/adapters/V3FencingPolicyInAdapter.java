/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.FencingPolicy;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3FencingPolicy;

public class V3FencingPolicyInAdapter implements V3Adapter<V3FencingPolicy, FencingPolicy> {
    @Override
    public FencingPolicy adapt(V3FencingPolicy from) {
        FencingPolicy to = new FencingPolicy();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        if (from.isSetSkipIfConnectivityBroken()) {
            to.setSkipIfConnectivityBroken(adaptIn(from.getSkipIfConnectivityBroken()));
        }
        if (from.isSetSkipIfSdActive()) {
            to.setSkipIfSdActive(adaptIn(from.getSkipIfSdActive()));
        }
        return to;
    }
}
