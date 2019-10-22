/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.OpenStackVolumeProviders;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3OpenStackVolumeProviders;

public class V3OpenStackVolumeProvidersOutAdapter implements V3Adapter<OpenStackVolumeProviders, V3OpenStackVolumeProviders> {
    @Override
    public V3OpenStackVolumeProviders adapt(OpenStackVolumeProviders from) {
        V3OpenStackVolumeProviders to = new V3OpenStackVolumeProviders();
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
        to.getOpenStackVolumeProviders().addAll(adaptOut(from.getOpenStackVolumeProviders()));
        return to;
    }
}
