/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.NetworkConfiguration;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3NetworkConfiguration;
import org.ovirt.engine.api.v3.types.V3Nics;

public class V3NetworkConfigurationOutAdapter implements V3Adapter<NetworkConfiguration, V3NetworkConfiguration> {
    @Override
    public V3NetworkConfiguration adapt(NetworkConfiguration from) {
        V3NetworkConfiguration to = new V3NetworkConfiguration();
        if (from.isSetDns()) {
            to.setDns(adaptOut(from.getDns()));
        }
        if (from.isSetNics()) {
            to.setNics(new V3Nics());
            to.getNics().getNics().addAll(adaptOut(from.getNics().getNics()));
        }
        return to;
    }
}
