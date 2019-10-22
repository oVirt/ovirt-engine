/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.GlusterServerHooks;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterServerHooks;

public class V3GlusterServerHooksOutAdapter implements V3Adapter<GlusterServerHooks, V3GlusterServerHooks> {
    @Override
    public V3GlusterServerHooks adapt(GlusterServerHooks from) {
        V3GlusterServerHooks to = new V3GlusterServerHooks();
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
        to.getGlusterServerHooks().addAll(adaptOut(from.getGlusterServerHooks()));
        return to;
    }
}
