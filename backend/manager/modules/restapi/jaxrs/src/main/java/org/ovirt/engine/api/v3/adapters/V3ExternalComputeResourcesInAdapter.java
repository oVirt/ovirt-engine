/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.ExternalComputeResources;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ExternalComputeResources;

public class V3ExternalComputeResourcesInAdapter implements V3Adapter<V3ExternalComputeResources, ExternalComputeResources> {
    @Override
    public ExternalComputeResources adapt(V3ExternalComputeResources from) {
        ExternalComputeResources to = new ExternalComputeResources();
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
        to.getExternalComputeResources().addAll(adaptIn(from.getExternalComputeResources()));
        return to;
    }
}
