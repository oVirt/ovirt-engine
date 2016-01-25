/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import java.util.List;

import org.ovirt.engine.api.model.Boot;
import org.ovirt.engine.api.model.OperatingSystem;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Boot;
import org.ovirt.engine.api.v3.types.V3OperatingSystem;

public class V3OperatingSystemOutAdapter implements V3Adapter<OperatingSystem, V3OperatingSystem> {
    @Override
    public V3OperatingSystem adapt(OperatingSystem from) {
        V3OperatingSystem to = new V3OperatingSystem();
        Boot fromBoot = from.getBoot();
        if (fromBoot != null) {
            if (fromBoot.isSetDevices()) {
                List<V3Boot> toBoot = to.getBoot();
                fromBoot.getDevices().getDevices().stream().forEach(device -> {
                    V3Boot boot = new V3Boot();
                    boot.setDev(device.value());
                    toBoot.add(boot);
                });
            }
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
            to.setVersion(adaptOut(from.getVersion()));
        }
        return to;
    }
}
