/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.PmProxy;
import org.ovirt.engine.api.model.PmProxyType;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3PmProxy;

public class V3PmProxyInAdapter implements V3Adapter<V3PmProxy, PmProxy> {
    @Override
    public PmProxy adapt(V3PmProxy from) {
        PmProxy to = new PmProxy();
        if (from.isSetType()) {
            to.setType(PmProxyType.fromValue(from.getType()));
        }
        return to;
    }
}
