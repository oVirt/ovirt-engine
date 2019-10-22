/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.UnmanagedNetworks;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3UnmanagedNetworks;

public class V3UnmanagedNetworksInAdapter implements V3Adapter<V3UnmanagedNetworks, UnmanagedNetworks> {
    @Override
    public UnmanagedNetworks adapt(V3UnmanagedNetworks from) {
        UnmanagedNetworks to = new UnmanagedNetworks();
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
        to.getUnmanagedNetworks().addAll(adaptIn(from.getUnmanagedNetworks()));
        return to;
    }
}
