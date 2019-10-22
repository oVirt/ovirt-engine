/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.GraphicsConsoles;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GraphicsConsoles;

public class V3GraphicsConsolesOutAdapter implements V3Adapter<GraphicsConsoles, V3GraphicsConsoles> {
    @Override
    public V3GraphicsConsoles adapt(GraphicsConsoles from) {
        V3GraphicsConsoles to = new V3GraphicsConsoles();
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
        to.getGraphicsConsoles().addAll(adaptOut(from.getGraphicsConsoles()));
        return to;
    }
}
