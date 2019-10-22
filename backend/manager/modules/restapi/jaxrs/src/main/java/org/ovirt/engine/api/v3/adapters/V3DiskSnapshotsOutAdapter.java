/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.DiskSnapshots;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3DiskSnapshots;

public class V3DiskSnapshotsOutAdapter implements V3Adapter<DiskSnapshots, V3DiskSnapshots> {
    @Override
    public V3DiskSnapshots adapt(DiskSnapshots from) {
        V3DiskSnapshots to = new V3DiskSnapshots();
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
        to.getDiskSnapshots().addAll(adaptOut(from.getDiskSnapshots()));
        return to;
    }
}
