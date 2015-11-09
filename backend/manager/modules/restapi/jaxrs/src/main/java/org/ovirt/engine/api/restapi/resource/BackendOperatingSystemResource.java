/*
* Copyright (c) 2014 Red Hat, Inc.
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

import org.ovirt.engine.api.model.OperatingSystemInfo;
import org.ovirt.engine.api.resource.OperatingSystemResource;
import org.ovirt.engine.api.restapi.util.IconHelper;
import org.ovirt.engine.core.common.businessentities.VmIconDefault;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.GetVmIconDefaultParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;

public class BackendOperatingSystemResource
        extends AbstractBackendSubResource<OperatingSystemInfo, Integer>
        implements OperatingSystemResource {
    public BackendOperatingSystemResource(String id) {
        super(id, OperatingSystemInfo.class, Integer.class);
    }

    @Override
    public OperatingSystemInfo get() {
        OsRepository repository = SimpleDependencyInjector.getInstance().get(OsRepository.class);
        OperatingSystemInfo model = new OperatingSystemInfo();
        model.setId(id);
        Integer key = Integer.valueOf(id);
        String uniqueName = repository.getUniqueOsNames().get(key);
        if (uniqueName == null) {
            return notFound();
        }
        model.setName(uniqueName);
        String name = repository.getOsNames().get(key);
        if (name != null) {
            model.setDescription(name);
        }
        final VmIconDefault vmIconDefault = getEntity(VmIconDefault.class,
                VdcQueryType.GetVmIconDefault,
                new GetVmIconDefaultParameters(key),
                "VmIconDefault");
        if (vmIconDefault != null) {
            model.setSmallIcon(IconHelper.createIcon(vmIconDefault.getSmallIconId()));
            model.setLargeIcon(IconHelper.createIcon(vmIconDefault.getLargeIconId()));
        }
        return addLinks(model);
    }

    @Override
    protected Guid asGuidOr404(String id) {
        // The identifier if an operating system isn't a UUID.
        return null;
    }
}
