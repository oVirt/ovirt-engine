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

import org.ovirt.engine.api.model.GuestOperatingSystem;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GuestOperatingSystem;

public class V3GuestOperatingSystemOutAdapter implements V3Adapter<GuestOperatingSystem, V3GuestOperatingSystem> {
    @Override
    public V3GuestOperatingSystem adapt(GuestOperatingSystem from) {
        V3GuestOperatingSystem to = new V3GuestOperatingSystem();
        if (from.isSetArchitecture()) {
            to.setArchitecture(from.getArchitecture());
        }
        if (from.isSetCodename()) {
            to.setCodename(from.getCodename());
        }
        if (from.isSetDistribution()) {
            to.setDistribution(from.getDistribution());
        }
        if (from.isSetFamily()) {
            to.setFamily(from.getFamily());
        }
        if (from.isSetKernel()) {
            to.setKernel(adaptOut(from.getKernel()));
        }
        if (from.isSetVersion()) {
            to.setVersion(adaptOut(from.getVersion()));
        }
        return to;
    }
}
