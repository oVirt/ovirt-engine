/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.DiskProfiles;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3DiskProfiles;

public class V3DiskProfilesInAdapter implements V3Adapter<V3DiskProfiles, DiskProfiles> {
    @Override
    public DiskProfiles adapt(V3DiskProfiles from) {
        DiskProfiles to = new DiskProfiles();
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
        to.getDiskProfiles().addAll(adaptIn(from.getDiskProfiles()));
        return to;
    }
}
