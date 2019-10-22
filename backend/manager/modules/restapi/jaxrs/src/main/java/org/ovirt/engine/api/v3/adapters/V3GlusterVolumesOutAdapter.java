/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.GlusterVolumes;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterVolumes;

public class V3GlusterVolumesOutAdapter implements V3Adapter<GlusterVolumes, V3GlusterVolumes> {
    @Override
    public V3GlusterVolumes adapt(GlusterVolumes from) {
        V3GlusterVolumes to = new V3GlusterVolumes();
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
        to.getGlusterVolumes().addAll(adaptOut(from.getGlusterVolumes()));
        return to;
    }
}
