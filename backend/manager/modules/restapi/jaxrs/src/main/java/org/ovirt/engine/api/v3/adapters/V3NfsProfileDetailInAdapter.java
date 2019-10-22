/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.NfsProfileDetail;
import org.ovirt.engine.api.model.ProfileDetails;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3NfsProfileDetail;

public class V3NfsProfileDetailInAdapter implements V3Adapter<V3NfsProfileDetail, NfsProfileDetail> {
    @Override
    public NfsProfileDetail adapt(V3NfsProfileDetail from) {
        NfsProfileDetail to = new NfsProfileDetail();
        if (from.isSetNfsServerIp()) {
            to.setNfsServerIp(from.getNfsServerIp());
        }
        if (from.isSetProfileDetail()) {
            to.setProfileDetails(new ProfileDetails());
            to.getProfileDetails().getProfileDetails().addAll(adaptIn(from.getProfileDetail()));
        }
        return to;
    }
}
