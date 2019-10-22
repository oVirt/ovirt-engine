/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.InstanceTypes;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3InstanceTypes;

public class V3InstanceTypesOutAdapter implements V3Adapter<InstanceTypes, V3InstanceTypes> {
    @Override
    public V3InstanceTypes adapt(InstanceTypes from) {
        V3InstanceTypes to = new V3InstanceTypes();
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
        to.getInstanceTypes().addAll(adaptOut(from.getInstanceTypes()));
        return to;
    }
}
