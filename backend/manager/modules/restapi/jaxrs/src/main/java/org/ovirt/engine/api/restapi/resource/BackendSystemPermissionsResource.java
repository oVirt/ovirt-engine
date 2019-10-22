/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.resource.SystemPermissionsResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

/**
 * This resource corresponds to the permissions assigned to the system object.
 */
public class BackendSystemPermissionsResource extends BackendAssignedPermissionsResource
    implements SystemPermissionsResource{

    public BackendSystemPermissionsResource() {
        super(
            Guid.SYSTEM,
            QueryType.GetPermissionsForObject,
            new GetPermissionsForObjectParameters(Guid.SYSTEM),
            BaseResource.class,
            VdcObjectType.System
        );
    }

}
