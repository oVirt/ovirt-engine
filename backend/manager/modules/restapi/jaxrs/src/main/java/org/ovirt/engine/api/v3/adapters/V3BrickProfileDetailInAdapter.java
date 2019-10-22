/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.BrickProfileDetail;
import org.ovirt.engine.api.model.ProfileDetails;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3BrickProfileDetail;

public class V3BrickProfileDetailInAdapter implements V3Adapter<V3BrickProfileDetail, BrickProfileDetail> {
    @Override
    public BrickProfileDetail adapt(V3BrickProfileDetail from) {
        BrickProfileDetail to = new BrickProfileDetail();
        if (from.isSetBrick()) {
            to.setBrick(adaptIn(from.getBrick()));
        }
        if (from.isSetProfileDetail()) {
            to.setProfileDetails(new ProfileDetails());
            to.getProfileDetails().getProfileDetails().addAll(adaptIn(from.getProfileDetail()));
        }
        return to;
    }
}
