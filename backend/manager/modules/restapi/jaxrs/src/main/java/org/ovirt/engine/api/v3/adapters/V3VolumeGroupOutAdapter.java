/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.VolumeGroup;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VolumeGroup;

public class V3VolumeGroupOutAdapter implements V3Adapter<VolumeGroup, V3VolumeGroup> {
    @Override
    public V3VolumeGroup adapt(VolumeGroup from) {
        V3VolumeGroup to = new V3VolumeGroup();
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetLogicalUnits()) {
            to.getLogicalUnits().addAll(adaptOut(from.getLogicalUnits().getLogicalUnits()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        return to;
    }
}
