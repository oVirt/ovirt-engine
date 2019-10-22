/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.PmProxy;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3PmProxy;

public class V3PmProxyOutAdapter implements V3Adapter<PmProxy, V3PmProxy> {
    @Override
    public V3PmProxy adapt(PmProxy from) {
        V3PmProxy to = new V3PmProxy();
        if (from.isSetType()) {
            to.setType(from.getType().value());
        }
        return to;
    }
}
