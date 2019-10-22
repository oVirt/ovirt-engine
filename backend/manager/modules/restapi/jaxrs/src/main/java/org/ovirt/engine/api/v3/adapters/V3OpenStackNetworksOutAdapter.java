/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.OpenStackNetworks;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3OpenStackNetworks;

public class V3OpenStackNetworksOutAdapter implements V3Adapter<OpenStackNetworks, V3OpenStackNetworks> {
    @Override
    public V3OpenStackNetworks adapt(OpenStackNetworks from) {
        V3OpenStackNetworks to = new V3OpenStackNetworks();
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetActive()) {
            to.setActive(from.getActive());
        }
        if (from.isSetSize()) {
            to.setSize(from.getSize());
        }
        if (from.isSetTotal()) {
            to.setTotal(from.getTotal());
        }
        to.getOpenStackNetworks().addAll(adaptOut(from.getOpenStackNetworks()));
        return to;
    }
}
