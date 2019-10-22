/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.GuestOperatingSystem;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GuestOperatingSystem;

public class V3GuestOperatingSystemInAdapter implements V3Adapter<V3GuestOperatingSystem, GuestOperatingSystem> {
    @Override
    public GuestOperatingSystem adapt(V3GuestOperatingSystem from) {
        GuestOperatingSystem to = new GuestOperatingSystem();
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
            to.setKernel(adaptIn(from.getKernel()));
        }
        if (from.isSetVersion()) {
            to.setVersion(adaptIn(from.getVersion()));
        }
        return to;
    }
}
