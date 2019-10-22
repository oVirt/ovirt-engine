/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.StorageConnections;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3StorageConnections;

public class V3StorageConnectionsOutAdapter implements V3Adapter<StorageConnections, V3StorageConnections> {
    @Override
    public V3StorageConnections adapt(StorageConnections from) {
        V3StorageConnections to = new V3StorageConnections();
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
        to.getStorageConnections().addAll(adaptOut(from.getStorageConnections()));
        return to;
    }
}
