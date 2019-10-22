/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.OpenStackNetworkProviders;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3OpenStackNetworkProviders;

public class V3OpenStackNetworkProvidersOutAdapter implements V3Adapter<OpenStackNetworkProviders, V3OpenStackNetworkProviders> {
    @Override
    public V3OpenStackNetworkProviders adapt(OpenStackNetworkProviders from) {
        V3OpenStackNetworkProviders to = new V3OpenStackNetworkProviders();
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
        to.getOpenStackNetworkProviders().addAll(adaptOut(from.getOpenStackNetworkProviders()));
        return to;
    }
}
