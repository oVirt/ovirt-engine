/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Files;
import org.ovirt.engine.api.model.Payload;
import org.ovirt.engine.api.model.VmDeviceType;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Payload;

public class V3PayloadInAdapter implements V3Adapter<V3Payload, Payload> {
    @Override
    public Payload adapt(V3Payload from) {
        Payload to = new Payload();
        if (from.isSetFiles()) {
            to.setFiles(new Files());
            to.getFiles().getFiles().addAll(adaptIn(from.getFiles().getFiles()));
        }
        to.setType(VmDeviceType.fromValue(from.getType()));
        if (from.isSetVolumeId()) {
            to.setVolumeId(from.getVolumeId());
        }
        return to;
    }
}
