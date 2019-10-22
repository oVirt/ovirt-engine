/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.HostNicVirtualFunctionsConfiguration;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3HostNicVirtualFunctionsConfiguration;

public class V3HostNicVirtualFunctionsConfigurationInAdapter implements V3Adapter<V3HostNicVirtualFunctionsConfiguration, HostNicVirtualFunctionsConfiguration> {
    @Override
    public HostNicVirtualFunctionsConfiguration adapt(V3HostNicVirtualFunctionsConfiguration from) {
        HostNicVirtualFunctionsConfiguration to = new HostNicVirtualFunctionsConfiguration();
        if (from.isSetAllNetworksAllowed()) {
            to.setAllNetworksAllowed(from.isAllNetworksAllowed());
        }
        if (from.isSetMaxNumberOfVirtualFunctions()) {
            to.setMaxNumberOfVirtualFunctions(from.getMaxNumberOfVirtualFunctions());
        }
        if (from.isSetNumberOfVirtualFunctions()) {
            to.setNumberOfVirtualFunctions(from.getNumberOfVirtualFunctions());
        }
        return to;
    }
}
