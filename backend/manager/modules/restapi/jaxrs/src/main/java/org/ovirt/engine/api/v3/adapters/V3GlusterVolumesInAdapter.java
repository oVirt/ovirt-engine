/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.GlusterVolumes;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterVolumes;

public class V3GlusterVolumesInAdapter implements V3Adapter<V3GlusterVolumes, GlusterVolumes> {
    @Override
    public GlusterVolumes adapt(V3GlusterVolumes from) {
        GlusterVolumes to = new GlusterVolumes();
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
        to.getGlusterVolumes().addAll(adaptIn(from.getGlusterVolumes()));
        return to;
    }
}
