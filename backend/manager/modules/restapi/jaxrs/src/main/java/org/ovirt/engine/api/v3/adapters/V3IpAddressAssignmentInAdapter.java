/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.IpAddressAssignment;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3IpAddressAssignment;

public class V3IpAddressAssignmentInAdapter implements V3Adapter<V3IpAddressAssignment, IpAddressAssignment> {
    @Override
    public IpAddressAssignment adapt(V3IpAddressAssignment from) {
        IpAddressAssignment to = new IpAddressAssignment();
        if (from.isSetAssignmentMethod()) {
            to.setAssignmentMethod(BootProtocol.fromValue(from.getAssignmentMethod()));
        }
        if (from.isSetIp()) {
            to.setIp(adaptIn(from.getIp()));
        }
        return to;
    }
}
