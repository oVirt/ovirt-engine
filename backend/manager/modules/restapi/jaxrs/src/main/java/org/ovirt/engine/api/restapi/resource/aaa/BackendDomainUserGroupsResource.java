/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource.aaa;

import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Groups;
import org.ovirt.engine.api.resource.aaa.DomainUserGroupsResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.core.common.businessentities.aaa.AuthzGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDomainUserGroupsResource extends AbstractBackendCollectionResource<Group, AuthzGroup>
        implements DomainUserGroupsResource {

    private Guid userId;

    public BackendDomainUserGroupsResource(Guid userId) {
        super(Group.class, AuthzGroup.class);
        this.userId = userId;
    }

    @Override
    public Groups list() {
        return mapGroups(getBackendCollection(QueryType.GetAuthzGroupsByUserId, new IdQueryParameters(userId)));
    }

    private Groups mapGroups(List<AuthzGroup> authzGroups) {
        Groups groups = new Groups();
        groups.getGroups().addAll(
            authzGroups.stream()
                .map(g -> map(g))
                .map(g -> addLinks(g))
                .collect(Collectors.toList())
        );

        return groups;
    }

}
