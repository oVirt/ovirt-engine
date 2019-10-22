/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Vlan;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VLAN;

public class V3VLANInAdapter implements V3Adapter<V3VLAN, Vlan> {
    @Override
    public Vlan adapt(V3VLAN from) {
        Vlan to = new Vlan();
        to.setId(from.getId());
        return to;
    }
}
