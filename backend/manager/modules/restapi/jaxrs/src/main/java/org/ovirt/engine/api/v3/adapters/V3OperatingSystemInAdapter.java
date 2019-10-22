/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import java.util.List;

import org.ovirt.engine.api.model.Boot;
import org.ovirt.engine.api.model.BootDevice;
import org.ovirt.engine.api.model.OperatingSystem;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Boot;
import org.ovirt.engine.api.v3.types.V3OperatingSystem;

public class V3OperatingSystemInAdapter implements V3Adapter<V3OperatingSystem, OperatingSystem> {
    @Override
    public OperatingSystem adapt(V3OperatingSystem from) {
        OperatingSystem to = new OperatingSystem();
        if (from.isSetBoot()) {
            Boot toBoot = new Boot();
            Boot.DevicesList toDevicesList = new Boot.DevicesList();
            List<BootDevice> toDevices = toDevicesList.getDevices();
            from.getBoot().stream()
                .map(V3Boot::getDev)
                .map(BootDevice::fromValue)
                .forEach(toDevices::add);
            toBoot.setDevices(toDevicesList);
            to.setBoot(toBoot);
        }
        if (from.isSetCmdline()) {
            to.setCmdline(from.getCmdline());
        }
        if (from.isSetInitrd()) {
            to.setInitrd(from.getInitrd());
        }
        if (from.isSetKernel()) {
            to.setKernel(from.getKernel());
        }
        if (from.isSetType()) {
            to.setType(from.getType());
        }
        if (from.isSetVersion()) {
            to.setVersion(adaptIn(from.getVersion()));
        }
        return to;
    }
}
