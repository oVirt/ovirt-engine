/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.UnmanagedNetworks;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3UnmanagedNetworks;

public class V3UnmanagedNetworksOutAdapter implements V3Adapter<UnmanagedNetworks, V3UnmanagedNetworks> {
    @Override
    public V3UnmanagedNetworks adapt(UnmanagedNetworks from) {
        V3UnmanagedNetworks to = new V3UnmanagedNetworks();
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
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
        to.getUnmanagedNetworks().addAll(adaptOut(from.getUnmanagedNetworks()));
        return to;
    }
}
