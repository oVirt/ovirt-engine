/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.ReportedDevices;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ReportedDevices;

public class V3ReportedDevicesOutAdapter implements V3Adapter<ReportedDevices, V3ReportedDevices> {
    @Override
    public V3ReportedDevices adapt(ReportedDevices from) {
        V3ReportedDevices to = new V3ReportedDevices();
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
        to.getReportedDevices().addAll(adaptOut(from.getReportedDevices()));
        return to;
    }
}
