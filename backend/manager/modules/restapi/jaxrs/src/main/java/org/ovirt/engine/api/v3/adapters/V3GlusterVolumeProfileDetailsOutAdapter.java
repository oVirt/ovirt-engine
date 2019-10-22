/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.GlusterVolumeProfileDetails;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3BrickProfileDetails;
import org.ovirt.engine.api.v3.types.V3GlusterVolumeProfileDetails;
import org.ovirt.engine.api.v3.types.V3NfsProfileDetails;

public class V3GlusterVolumeProfileDetailsOutAdapter implements V3Adapter<GlusterVolumeProfileDetails, V3GlusterVolumeProfileDetails> {
    @Override
    public V3GlusterVolumeProfileDetails adapt(GlusterVolumeProfileDetails from) {
        V3GlusterVolumeProfileDetails to = new V3GlusterVolumeProfileDetails();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetBrickProfileDetails()) {
            to.setBrickProfileDetails(new V3BrickProfileDetails());
            to.getBrickProfileDetails().getBrickProfileDetail().addAll(adaptOut(from.getBrickProfileDetails().getBrickProfileDetails()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetNfsProfileDetails()) {
            to.setNfsProfileDetails(new V3NfsProfileDetails());
            to.getNfsProfileDetails().getNfsProfileDetail().addAll(adaptOut(from.getNfsProfileDetails().getNfsProfileDetails()));
        }
        return to;
    }
}
