/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.GlusterServerHook;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterServerHook;
import org.ovirt.engine.api.v3.types.V3Status;

public class V3GlusterServerHookOutAdapter implements V3Adapter<GlusterServerHook, V3GlusterServerHook> {
    @Override
    public V3GlusterServerHook adapt(GlusterServerHook from) {
        V3GlusterServerHook to = new V3GlusterServerHook();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetChecksum()) {
            to.setChecksum(from.getChecksum());
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetContentType()) {
            to.setContentType(from.getContentType().value());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetHost()) {
            to.setHost(adaptOut(from.getHost()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetStatus()) {
            V3Status status = new V3Status();
            status.setState(from.getStatus().value());
            to.setStatus(status);
        }
        return to;
    }
}
