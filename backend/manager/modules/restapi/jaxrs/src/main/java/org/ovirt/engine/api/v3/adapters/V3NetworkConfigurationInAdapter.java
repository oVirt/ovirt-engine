/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.NetworkConfiguration;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3NetworkConfiguration;

public class V3NetworkConfigurationInAdapter implements V3Adapter<V3NetworkConfiguration, NetworkConfiguration> {
    @Override
    public NetworkConfiguration adapt(V3NetworkConfiguration from) {
        NetworkConfiguration to = new NetworkConfiguration();
        if (from.isSetDns()) {
            to.setDns(adaptIn(from.getDns()));
        }
        if (from.isSetNics()) {
            to.setNics(new Nics());
            to.getNics().getNics().addAll(adaptIn(from.getNics().getNics()));
        }
        return to;
    }
}
