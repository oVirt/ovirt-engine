/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.FencingPolicy;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3FencingPolicy;

public class V3FencingPolicyOutAdapter implements V3Adapter<FencingPolicy, V3FencingPolicy> {
    @Override
    public V3FencingPolicy adapt(FencingPolicy from) {
        V3FencingPolicy to = new V3FencingPolicy();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        if (from.isSetSkipIfConnectivityBroken()) {
            to.setSkipIfConnectivityBroken(adaptOut(from.getSkipIfConnectivityBroken()));
        }
        if (from.isSetSkipIfSdActive()) {
            to.setSkipIfSdActive(adaptOut(from.getSkipIfSdActive()));
        }
        return to;
    }
}
