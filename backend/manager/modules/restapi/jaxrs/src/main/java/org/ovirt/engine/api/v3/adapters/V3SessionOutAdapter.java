/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Session;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Session;

public class V3SessionOutAdapter implements V3Adapter<Session, V3Session> {
    @Override
    public V3Session adapt(Session from) {
        V3Session to = new V3Session();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
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
            to.setIp(adaptOut(from.getIp()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetProtocol()) {
            to.setProtocol(from.getProtocol());
        }
        if (from.isSetUser()) {
            to.setUser(adaptOut(from.getUser()));
        }
        if (from.isSetVm()) {
            to.setVm(adaptOut(from.getVm()));
        }
        return to;
    }
}
