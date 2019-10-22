/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Ip;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3IP;

public class V3IpOutAdapter implements V3Adapter<Ip, V3IP> {
    @Override
    public V3IP adapt(Ip from) {
        V3IP to = new V3IP();
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
            to.setVersion(from.getVersion().value());
        }
        return to;
    }
}
