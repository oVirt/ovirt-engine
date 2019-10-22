/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.StorageConnections;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3StorageConnections;

public class V3StorageConnectionsInAdapter implements V3Adapter<V3StorageConnections, StorageConnections> {
    @Override
    public StorageConnections adapt(V3StorageConnections from) {
        StorageConnections to = new StorageConnections();
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
        to.getStorageConnections().addAll(adaptIn(from.getStorageConnections()));
        return to;
    }
}
