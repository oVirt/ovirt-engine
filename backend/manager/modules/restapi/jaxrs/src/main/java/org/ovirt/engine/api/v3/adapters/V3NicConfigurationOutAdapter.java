/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.NicConfiguration;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GuestNicConfiguration;

public class V3NicConfigurationOutAdapter implements V3Adapter<NicConfiguration, V3GuestNicConfiguration> {
    @Override
    public V3GuestNicConfiguration adapt(NicConfiguration from) {
        V3GuestNicConfiguration to = new V3GuestNicConfiguration();
        if (from.isSetBootProtocol()) {
            to.setBootProtocol(from.getBootProtocol().value());
        }
        if (from.isSetIp()) {
            to.setIp(adaptOut(from.getIp()));
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
