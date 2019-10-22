/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.HostDevices;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3HostDevices;

public class V3HostDevicesInAdapter implements V3Adapter<V3HostDevices, HostDevices> {
    @Override
    public HostDevices adapt(V3HostDevices from) {
        HostDevices to = new HostDevices();
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetActive()) {
            to.setActive(from.getActive());
        }
        if (from.isSetSize()) {
            to.setSize(from.getSize());
        }
        if (from.isSetTotal()) {
            to.setTotal(from.getTotal());
        }
        to.getHostDevices().addAll(adaptIn(from.getHostDevices()));
        return to;
    }
}
