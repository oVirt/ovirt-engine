/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.ExternalComputeResources;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ExternalComputeResources;

public class V3ExternalComputeResourcesOutAdapter implements V3Adapter<ExternalComputeResources, V3ExternalComputeResources> {
    @Override
    public V3ExternalComputeResources adapt(ExternalComputeResources from) {
        V3ExternalComputeResources to = new V3ExternalComputeResources();
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
        to.getExternalComputeResources().addAll(adaptOut(from.getExternalComputeResources()));
        return to;
    }
}
