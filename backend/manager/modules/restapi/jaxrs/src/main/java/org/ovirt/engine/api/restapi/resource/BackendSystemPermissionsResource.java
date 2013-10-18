/*
* Copyright (c) 2013 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.resource.SystemPermissionsResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

/**
 * This resource corresponds to the permissions assigned to the system object.
 */
public class BackendSystemPermissionsResource extends BackendAssignedPermissionsResource
    implements SystemPermissionsResource{

    public BackendSystemPermissionsResource() {
        super(
            Guid.SYSTEM,
            VdcQueryType.GetPermissionsForObject,
            new GetPermissionsForObjectParameters(Guid.SYSTEM),
            BaseResource.class,
            VdcObjectType.System
        );
    }

}
