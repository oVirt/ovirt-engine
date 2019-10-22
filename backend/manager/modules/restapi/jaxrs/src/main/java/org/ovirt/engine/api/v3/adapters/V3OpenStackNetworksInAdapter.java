/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.OpenStackNetworks;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3OpenStackNetworks;

public class V3OpenStackNetworksInAdapter implements V3Adapter<V3OpenStackNetworks, OpenStackNetworks> {
    @Override
    public OpenStackNetworks adapt(V3OpenStackNetworks from) {
        OpenStackNetworks to = new OpenStackNetworks();
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
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
        to.getOpenStackNetworks().addAll(adaptIn(from.getOpenStackNetworks()));
        return to;
    }
}
