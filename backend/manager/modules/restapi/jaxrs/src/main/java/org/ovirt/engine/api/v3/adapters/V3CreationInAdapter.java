/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Creation;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Creation;

public class V3CreationInAdapter implements V3Adapter<V3Creation, Creation> {
    @Override
    public Creation adapt(V3Creation from) {
        Creation to = new Creation();
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetStatus() && from.getStatus().isSetState()) {
            to.setStatus(from.getStatus().getState());
        }
        if (from.isSetFault()) {
            to.setFault(adaptIn(from.getFault()));
        }
        return to;
    }
}
