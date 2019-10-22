/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Payload;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Files;
import org.ovirt.engine.api.v3.types.V3Payload;

public class V3PayloadOutAdapter implements V3Adapter<Payload, V3Payload> {
    @Override
    public V3Payload adapt(Payload from) {
        V3Payload to = new V3Payload();
        if (from.isSetFiles()) {
            to.setFiles(new V3Files());
            to.getFiles().getFiles().addAll(adaptOut(from.getFiles().getFiles()));
        }
        if (from.isSetType()) {
            to.setType(from.getType().value());
        }
        if (from.isSetVolumeId()) {
            to.setVolumeId(from.getVolumeId());
        }
        return to;
    }
}
