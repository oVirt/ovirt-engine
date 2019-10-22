/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.EntityProfileDetail;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3EntityProfileDetail;

public class V3EntityProfileDetailOutAdapter implements V3Adapter<EntityProfileDetail, V3EntityProfileDetail> {
    @Override
    public V3EntityProfileDetail adapt(EntityProfileDetail from) {
        V3EntityProfileDetail to = new V3EntityProfileDetail();
        if (from.isSetProfileDetails()) {
            to.getProfileDetail().addAll(adaptOut(from.getProfileDetails().getProfileDetails()));
        }
        return to;
    }
}
