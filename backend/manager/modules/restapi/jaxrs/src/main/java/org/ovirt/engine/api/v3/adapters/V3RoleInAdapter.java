/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Permits;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Role;

public class V3RoleInAdapter implements V3Adapter<V3Role, Role> {
    @Override
    public Role adapt(V3Role from) {
        Role to = new Role();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetAdministrative()) {
            to.setAdministrative(from.isAdministrative());
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
        if (from.isSetMutable()) {
            to.setMutable(from.isMutable());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetPermits()) {
            to.setPermits(new Permits());
            to.getPermits().getPermits().addAll(adaptIn(from.getPermits().getPermits()));
        }
        if (from.isSetUser()) {
            to.setUser(adaptIn(from.getUser()));
        }
        return to;
    }
}
