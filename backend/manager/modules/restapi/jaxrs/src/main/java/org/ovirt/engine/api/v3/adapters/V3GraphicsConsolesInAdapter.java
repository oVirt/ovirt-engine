/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.GraphicsConsoles;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GraphicsConsoles;

public class V3GraphicsConsolesInAdapter implements V3Adapter<V3GraphicsConsoles, GraphicsConsoles> {
    @Override
    public GraphicsConsoles adapt(V3GraphicsConsoles from) {
        GraphicsConsoles to = new GraphicsConsoles();
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
        to.getGraphicsConsoles().addAll(adaptIn(from.getGraphicsConsoles()));
        return to;
    }
}
