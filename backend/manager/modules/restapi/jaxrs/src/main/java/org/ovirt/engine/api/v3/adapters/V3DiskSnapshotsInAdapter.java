/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.DiskSnapshots;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3DiskSnapshots;

public class V3DiskSnapshotsInAdapter implements V3Adapter<V3DiskSnapshots, DiskSnapshots> {
    @Override
    public DiskSnapshots adapt(V3DiskSnapshots from) {
        DiskSnapshots to = new DiskSnapshots();
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
        to.getDiskSnapshots().addAll(adaptIn(from.getDiskSnapshots()));
        return to;
    }
}
