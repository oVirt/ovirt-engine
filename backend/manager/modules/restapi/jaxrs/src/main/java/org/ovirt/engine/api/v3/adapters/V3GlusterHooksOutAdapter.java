/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.GlusterHooks;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterHooks;

public class V3GlusterHooksOutAdapter implements V3Adapter<GlusterHooks, V3GlusterHooks> {
    @Override
    public V3GlusterHooks adapt(GlusterHooks from) {
        V3GlusterHooks to = new V3GlusterHooks();
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
        to.getGlusterHooks().addAll(adaptOut(from.getGlusterHooks()));
        return to;
    }
}
