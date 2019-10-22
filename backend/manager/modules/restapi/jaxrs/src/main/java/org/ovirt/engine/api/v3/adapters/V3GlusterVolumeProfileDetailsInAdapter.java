/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.BrickProfileDetails;
import org.ovirt.engine.api.model.GlusterVolumeProfileDetails;
import org.ovirt.engine.api.model.NfsProfileDetails;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterVolumeProfileDetails;

public class V3GlusterVolumeProfileDetailsInAdapter implements V3Adapter<V3GlusterVolumeProfileDetails, GlusterVolumeProfileDetails> {
    @Override
    public GlusterVolumeProfileDetails adapt(V3GlusterVolumeProfileDetails from) {
        GlusterVolumeProfileDetails to = new GlusterVolumeProfileDetails();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetBrickProfileDetails()) {
            to.setBrickProfileDetails(new BrickProfileDetails());
            to.getBrickProfileDetails().getBrickProfileDetails().addAll(adaptIn(from.getBrickProfileDetails().getBrickProfileDetail()));
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
            to.setNfsProfileDetails(new NfsProfileDetails());
            to.getNfsProfileDetails().getNfsProfileDetails().addAll(adaptIn(from.getNfsProfileDetails().getNfsProfileDetail()));
        }
        return to;
    }
}
