/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.GraphicsConsole;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GraphicsConsole;

public class V3GraphicsConsoleOutAdapter implements V3Adapter<GraphicsConsole, V3GraphicsConsole> {
    @Override
    public V3GraphicsConsole adapt(GraphicsConsole from) {
        V3GraphicsConsole to = new V3GraphicsConsole();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
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
            to.setInstanceType(adaptOut(from.getInstanceType()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetPort()) {
            to.setPort(from.getPort());
        }
        if (from.isSetProtocol()) {
            to.setProtocol(from.getProtocol().value());
        }
        if (from.isSetTemplate()) {
            to.setTemplate(adaptOut(from.getTemplate()));
        }
        if (from.isSetTlsPort()) {
            to.setTlsPort(from.getTlsPort());
        }
        if (from.isSetVm()) {
            to.setVm(adaptOut(from.getVm()));
        }
        return to;
    }
}
