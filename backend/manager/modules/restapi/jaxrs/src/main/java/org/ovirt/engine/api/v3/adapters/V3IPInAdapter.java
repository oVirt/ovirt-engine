/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Ip;
import org.ovirt.engine.api.model.IpVersion;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3IP;

public class V3IPInAdapter implements V3Adapter<V3IP, Ip> {
    @Override
    public Ip adapt(V3IP from) {
        Ip to = new Ip();
        if (from.isSetAddress()) {
            to.setAddress(from.getAddress());
        }
        if (from.isSetGateway()) {
            to.setGateway(from.getGateway());
        }
        if (from.isSetNetmask()) {
            to.setNetmask(from.getNetmask());
        }
        if (from.isSetVersion()) {
            to.setVersion(IpVersion.fromValue(from.getVersion()));
        }
        return to;
    }
}
