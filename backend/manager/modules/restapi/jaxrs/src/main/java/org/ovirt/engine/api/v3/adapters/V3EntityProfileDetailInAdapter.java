/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.EntityProfileDetail;
import org.ovirt.engine.api.model.ProfileDetails;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3EntityProfileDetail;

public class V3EntityProfileDetailInAdapter implements V3Adapter<V3EntityProfileDetail, EntityProfileDetail> {
    @Override
    public EntityProfileDetail adapt(V3EntityProfileDetail from) {
        EntityProfileDetail to = new EntityProfileDetail();
        if (from.isSetProfileDetail()) {
            to.setProfileDetails(new ProfileDetails());
            to.getProfileDetails().getProfileDetails().addAll(adaptIn(from.getProfileDetail()));
        }
        return to;
    }
}
