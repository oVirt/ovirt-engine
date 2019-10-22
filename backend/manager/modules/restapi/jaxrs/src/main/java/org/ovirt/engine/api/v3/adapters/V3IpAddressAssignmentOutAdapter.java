/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.IpAddressAssignment;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3IpAddressAssignment;

public class V3IpAddressAssignmentOutAdapter implements V3Adapter<IpAddressAssignment, V3IpAddressAssignment> {
    @Override
    public V3IpAddressAssignment adapt(IpAddressAssignment from) {
        V3IpAddressAssignment to = new V3IpAddressAssignment();
        if (from.isSetAssignmentMethod()) {
            to.setAssignmentMethod(from.getAssignmentMethod().value());
        }
        if (from.isSetIp()) {
            to.setIp(adaptOut(from.getIp()));
        }
        return to;
    }
}
