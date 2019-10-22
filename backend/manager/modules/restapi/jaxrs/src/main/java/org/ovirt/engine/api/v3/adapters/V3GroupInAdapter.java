/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Roles;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Group;

public class V3GroupInAdapter implements V3Adapter<V3Group, Group> {
    @Override
    public Group adapt(V3Group from) {
        Group to = new Group();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetDomain()) {
            to.setDomain(adaptIn(from.getDomain()));
        }
        if (from.isSetDomainEntryId()) {
            to.setDomainEntryId(from.getDomainEntryId());
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
        if (from.isSetNamespace()) {
            to.setNamespace(from.getNamespace());
        }
        if (from.isSetRoles()) {
            to.setRoles(new Roles());
            to.getRoles().getRoles().addAll(adaptIn(from.getRoles().getRoles()));
        }
        return to;
    }
}
