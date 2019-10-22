/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Permits;
import org.ovirt.engine.api.v3.types.V3Role;

public class V3RoleOutAdapter implements V3Adapter<Role, V3Role> {
    @Override
    public V3Role adapt(Role from) {
        V3Role to = new V3Role();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
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
            to.setPermits(new V3Permits());
            to.getPermits().getPermits().addAll(adaptOut(from.getPermits().getPermits()));
        }
        if (from.isSetUser()) {
            to.setUser(adaptOut(from.getUser()));
        }
        return to;
    }
}
