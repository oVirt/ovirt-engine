/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.InstanceTypes;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3InstanceTypes;

public class V3InstanceTypesInAdapter implements V3Adapter<V3InstanceTypes, InstanceTypes> {
    @Override
    public InstanceTypes adapt(V3InstanceTypes from) {
        InstanceTypes to = new InstanceTypes();
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
        to.getInstanceTypes().addAll(adaptIn(from.getInstanceTypes()));
        return to;
    }
}
