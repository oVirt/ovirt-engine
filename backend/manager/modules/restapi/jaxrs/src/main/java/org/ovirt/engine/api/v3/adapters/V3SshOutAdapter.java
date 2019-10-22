/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Ssh;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3SSH;

public class V3SshOutAdapter implements V3Adapter<Ssh, V3SSH> {
    @Override
    public V3SSH adapt(Ssh from) {
        V3SSH to = new V3SSH();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetAuthenticationMethod()) {
            to.setAuthenticationMethod(from.getAuthenticationMethod().value());
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetFingerprint()) {
            to.setFingerprint(from.getFingerprint());
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
        if (from.isSetPort()) {
            to.setPort(from.getPort());
        }
        if (from.isSetUser()) {
            to.setUser(adaptOut(from.getUser()));
        }
        return to;
    }
}
