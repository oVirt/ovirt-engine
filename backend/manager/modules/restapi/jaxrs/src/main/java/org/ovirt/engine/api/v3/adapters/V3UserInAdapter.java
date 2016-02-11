/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Groups;
import org.ovirt.engine.api.model.Roles;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3User;

public class V3UserInAdapter implements V3Adapter<V3User, User> {
    @Override
    public User adapt(V3User from) {
        User to = new User();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
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
            to.setDomain(adaptIn(from.getDomain()));
        }
        if (from.isSetDomainEntryId()) {
            to.setDomainEntryId(from.getDomainEntryId());
        }
        if (from.isSetEmail()) {
            to.setEmail(from.getEmail());
        }
        if (from.isSetGroups()) {
            to.setGroups(new Groups());
            to.getGroups().getGroups().addAll(adaptIn(from.getGroups().getGroups()));
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
            to.setRoles(new Roles());
            to.getRoles().getRoles().addAll(adaptIn(from.getRoles().getRoles()));
        }
        if (from.isSetUserName()) {
            to.setUserName(from.getUserName());
        }
        return to;
    }
}
