/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.NicConfiguration;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GuestNicConfiguration;

public class V3GuestNicConfigurationInAdapter implements V3Adapter<V3GuestNicConfiguration, NicConfiguration> {
    @Override
    public NicConfiguration adapt(V3GuestNicConfiguration from) {
        NicConfiguration to = new NicConfiguration();
        if (from.isSetBootProtocol()) {
            to.setBootProtocol(BootProtocol.fromValue(from.getBootProtocol()));
        }
        if (from.isSetIp()) {
            to.setIp(adaptIn(from.getIp()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetOnBoot()) {
            to.setOnBoot(from.isOnBoot());
        }
        return to;
    }
}
