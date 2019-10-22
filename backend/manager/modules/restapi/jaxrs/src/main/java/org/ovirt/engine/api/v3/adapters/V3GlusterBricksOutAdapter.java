/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.GlusterBricks;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterBricks;

public class V3GlusterBricksOutAdapter implements V3Adapter<GlusterBricks, V3GlusterBricks> {
    @Override
    public V3GlusterBricks adapt(GlusterBricks from) {
        V3GlusterBricks to = new V3GlusterBricks();
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
        to.getGlusterBricks().addAll(adaptOut(from.getGlusterBricks()));
        return to;
    }
}
