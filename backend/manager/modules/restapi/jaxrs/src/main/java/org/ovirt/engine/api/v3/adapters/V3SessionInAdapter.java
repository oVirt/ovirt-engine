/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Session;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Session;

public class V3SessionInAdapter implements V3Adapter<V3Session, Session> {
    @Override
    public Session adapt(V3Session from) {
        Session to = new Session();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetConsoleUser()) {
            to.setConsoleUser(from.isConsoleUser());
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
        if (from.isSetIp()) {
            to.setIp(adaptIn(from.getIp()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetProtocol()) {
            to.setProtocol(from.getProtocol());
        }
        if (from.isSetUser()) {
            to.setUser(adaptIn(from.getUser()));
        }
        if (from.isSetVm()) {
            to.setVm(adaptIn(from.getVm()));
        }
        return to;
    }
}
