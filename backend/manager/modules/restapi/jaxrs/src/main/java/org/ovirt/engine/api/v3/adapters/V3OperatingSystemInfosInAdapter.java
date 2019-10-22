/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.OperatingSystemInfos;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3OperatingSystemInfos;

public class V3OperatingSystemInfosInAdapter implements V3Adapter<V3OperatingSystemInfos, OperatingSystemInfos> {
    @Override
    public OperatingSystemInfos adapt(V3OperatingSystemInfos from) {
        OperatingSystemInfos to = new OperatingSystemInfos();
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
        to.getOperatingSystemInfos().addAll(adaptIn(from.getOperatingSystemInfos()));
        return to;
    }
}
