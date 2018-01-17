/*
Copyright (c) 2017 Red Hat, Inc.

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

package org.ovirt.engine.core.bll;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.aaa.AuthzGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbGroupDao;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.PermissionDao;

public class GetPermissionsOnBehalfByAdElementIdQuery<P extends IdQueryParameters>
    extends QueriesCommandBase<P> {

    @Inject
    private PermissionDao permissionDao;

    @Inject
    private DbGroupDao dbGroupDao;

    @Inject
    private DbUserDao dbUserDao;

    public GetPermissionsOnBehalfByAdElementIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        QueryReturnValue returnValue = backend.runInternalQuery(QueryType.GetAuthzGroupsByUserId, getParameters());
        Collection<AuthzGroup> authzGroups = returnValue.getReturnValue();
        List<Guid>  groupsIds = authzGroups.stream()
            .map(g -> dbGroupDao.getByExternalId(g.getAuthz(), g.getId()))
            .filter(Objects::nonNull)
            .map(g -> g.getId())
            .collect(Collectors.toList());

        getQueryReturnValue().setReturnValue(
            permissionDao.getAllForAdElementAndGroups(
                getParameters().getId(),
                getUserID(),
                groupsIds,
                getParameters().isFiltered()
            )
        );
    }

}
