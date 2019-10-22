/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.GlusterServerHooks;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterServerHooks;

public class V3GlusterServerHooksInAdapter implements V3Adapter<V3GlusterServerHooks, GlusterServerHooks> {
    @Override
    public GlusterServerHooks adapt(V3GlusterServerHooks from) {
        GlusterServerHooks to = new GlusterServerHooks();
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
        to.getGlusterServerHooks().addAll(adaptIn(from.getGlusterServerHooks()));
        return to;
    }
}
