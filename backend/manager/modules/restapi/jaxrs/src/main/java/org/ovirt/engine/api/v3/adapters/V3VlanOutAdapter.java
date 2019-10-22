/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Vlan;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VLAN;

public class V3VlanOutAdapter implements V3Adapter<Vlan, V3VLAN> {
    @Override
    public V3VLAN adapt(Vlan from) {
        V3VLAN to = new V3VLAN();
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        return to;
    }
}
