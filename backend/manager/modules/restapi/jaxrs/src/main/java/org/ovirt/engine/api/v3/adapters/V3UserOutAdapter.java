/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Groups;
import org.ovirt.engine.api.v3.types.V3Roles;
import org.ovirt.engine.api.v3.types.V3User;

public class V3UserOutAdapter implements V3Adapter<User, V3User> {
    @Override
    public V3User adapt(User from) {
        V3User to = new V3User();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDepartment()) {
            to.setDepartment(from.getDepartment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetDomain()) {
            to.setDomain(adaptOut(from.getDomain()));
        }
        if (from.isSetDomainEntryId()) {
            to.setDomainEntryId(from.getDomainEntryId());
        }
        if (from.isSetEmail()) {
            to.setEmail(from.getEmail());
        }
        if (from.isSetGroups()) {
            to.setGroups(new V3Groups());
            to.getGroups().getGroups().addAll(adaptOut(from.getGroups().getGroups()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetLastName()) {
            to.setLastName(from.getLastName());
        }
        if (from.isSetLoggedIn()) {
            to.setLoggedIn(from.isLoggedIn());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetNamespace()) {
            to.setNamespace(from.getNamespace());
        }
        if (from.isSetPassword()) {
            to.setPassword(from.getPassword());
        }
        if (from.isSetPrincipal()) {
            to.setPrincipal(from.getPrincipal());
        }
        if (from.isSetRoles()) {
            to.setRoles(new V3Roles());
            to.getRoles().getRoles().addAll(adaptOut(from.getRoles().getRoles()));
        }
        if (from.isSetUserName()) {
            to.setUserName(from.getUserName());
        }
        return to;
    }
}
