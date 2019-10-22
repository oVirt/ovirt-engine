/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.GraphicsConsole;
import org.ovirt.engine.api.model.GraphicsType;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GraphicsConsole;

public class V3GraphicsConsoleInAdapter implements V3Adapter<V3GraphicsConsole, GraphicsConsole> {
    @Override
    public GraphicsConsole adapt(V3GraphicsConsole from) {
        GraphicsConsole to = new GraphicsConsole();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetAddress()) {
            to.setAddress(from.getAddress());
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetInstanceType()) {
            to.setInstanceType(adaptIn(from.getInstanceType()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetPort()) {
            to.setPort(from.getPort());
        }
        if (from.isSetProtocol()) {
            to.setProtocol(GraphicsType.fromValue(from.getProtocol()));
        }
        if (from.isSetTemplate()) {
            to.setTemplate(adaptIn(from.getTemplate()));
        }
        if (from.isSetTlsPort()) {
            to.setTlsPort(from.getTlsPort());
        }
        if (from.isSetVm()) {
            to.setVm(adaptIn(from.getVm()));
        }
        return to;
    }
}
