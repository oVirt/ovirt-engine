/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.OperatingSystemInfo;
import org.ovirt.engine.api.resource.OperatingSystemResource;
import org.ovirt.engine.api.restapi.types.CPUMapper;
import org.ovirt.engine.api.restapi.util.IconHelper;
import org.ovirt.engine.core.common.businessentities.VmIconDefault;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.GetVmIconDefaultParameters;
import org.ovirt.engine.core.common.queries.QueryType;
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
                QueryType.GetVmIconDefault,
                new GetVmIconDefaultParameters(key),
                "VmIconDefault");
        if (vmIconDefault != null) {
            model.setSmallIcon(IconHelper.createIcon(vmIconDefault.getSmallIconId()));
            model.setLargeIcon(IconHelper.createIcon(vmIconDefault.getLargeIconId()));
        }
        model.setArchitecture(CPUMapper.map(repository.getArchitectureFromOS(key), null));
        return addLinks(model);
    }

    @Override
    protected Guid asGuidOr404(String id) {
        // The identifier if an operating system isn't a UUID.
        return null;
    }
}
