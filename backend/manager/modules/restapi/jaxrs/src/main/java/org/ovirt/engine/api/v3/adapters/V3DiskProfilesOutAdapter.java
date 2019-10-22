/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.DiskProfiles;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3DiskProfiles;

public class V3DiskProfilesOutAdapter implements V3Adapter<DiskProfiles, V3DiskProfiles> {
    @Override
    public V3DiskProfiles adapt(DiskProfiles from) {
        V3DiskProfiles to = new V3DiskProfiles();
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
        to.getDiskProfiles().addAll(adaptOut(from.getDiskProfiles()));
        return to;
    }
}
