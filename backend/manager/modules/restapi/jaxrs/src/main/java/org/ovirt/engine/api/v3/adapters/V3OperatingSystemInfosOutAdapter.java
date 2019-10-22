/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.OperatingSystemInfos;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3OperatingSystemInfos;

public class V3OperatingSystemInfosOutAdapter implements V3Adapter<OperatingSystemInfos, V3OperatingSystemInfos> {
    @Override
    public V3OperatingSystemInfos adapt(OperatingSystemInfos from) {
        V3OperatingSystemInfos to = new V3OperatingSystemInfos();
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
        to.getOperatingSystemInfos().addAll(adaptOut(from.getOperatingSystemInfos()));
        return to;
    }
}
