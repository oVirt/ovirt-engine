/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.LogicalUnits;
import org.ovirt.engine.api.model.VolumeGroup;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VolumeGroup;

public class V3VolumeGroupInAdapter implements V3Adapter<V3VolumeGroup, VolumeGroup> {
    @Override
    public VolumeGroup adapt(V3VolumeGroup from) {
        VolumeGroup to = new VolumeGroup();
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetLogicalUnits()) {
            to.setLogicalUnits(new LogicalUnits());
            to.getLogicalUnits().getLogicalUnits().addAll(adaptIn(from.getLogicalUnits()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        return to;
    }
}
