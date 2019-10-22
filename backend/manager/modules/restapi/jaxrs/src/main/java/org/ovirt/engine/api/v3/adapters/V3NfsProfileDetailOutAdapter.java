/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.NfsProfileDetail;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3NfsProfileDetail;

public class V3NfsProfileDetailOutAdapter implements V3Adapter<NfsProfileDetail, V3NfsProfileDetail> {
    @Override
    public V3NfsProfileDetail adapt(NfsProfileDetail from) {
        V3NfsProfileDetail to = new V3NfsProfileDetail();
        if (from.isSetNfsServerIp()) {
            to.setNfsServerIp(from.getNfsServerIp());
        }
        if (from.isSetProfileDetails()) {
            to.getProfileDetail().addAll(adaptOut(from.getProfileDetails().getProfileDetails()));
        }
        return to;
    }
}
