/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.GlusterBricks;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterBricks;

public class V3GlusterBricksInAdapter implements V3Adapter<V3GlusterBricks, GlusterBricks> {
    @Override
    public GlusterBricks adapt(V3GlusterBricks from) {
        GlusterBricks to = new GlusterBricks();
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
        to.getGlusterBricks().addAll(adaptIn(from.getGlusterBricks()));
        return to;
    }
}
