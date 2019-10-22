/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Ssh;
import org.ovirt.engine.api.model.SshAuthenticationMethod;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3SSH;

public class V3SSHInAdapter implements V3Adapter<V3SSH, Ssh> {
    @Override
    public Ssh adapt(V3SSH from) {
        Ssh to = new Ssh();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetAuthenticationMethod()) {
            to.setAuthenticationMethod(SshAuthenticationMethod.fromValue(from.getAuthenticationMethod()));
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
            to.setUser(adaptIn(from.getUser()));
        }
        return to;
    }
}
