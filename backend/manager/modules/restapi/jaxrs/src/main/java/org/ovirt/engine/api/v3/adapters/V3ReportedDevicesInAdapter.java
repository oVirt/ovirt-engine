/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.ReportedDevices;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ReportedDevices;

public class V3ReportedDevicesInAdapter implements V3Adapter<V3ReportedDevices, ReportedDevices> {
    @Override
    public ReportedDevices adapt(V3ReportedDevices from) {
        ReportedDevices to = new ReportedDevices();
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
        to.getReportedDevices().addAll(adaptIn(from.getReportedDevices()));
        return to;
    }
}
