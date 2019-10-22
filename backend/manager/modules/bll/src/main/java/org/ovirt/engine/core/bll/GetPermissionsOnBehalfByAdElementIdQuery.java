/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
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
