/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.api.model.OperatingSystemInfo;
import org.ovirt.engine.api.model.OperatingSystemInfos;
import org.ovirt.engine.api.resource.OperatingSystemResource;
import org.ovirt.engine.api.resource.OperatingSystemsResource;
import org.ovirt.engine.api.restapi.types.CPUMapper;
import org.ovirt.engine.api.restapi.util.IconHelper;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.VmIconIdSizePair;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;

public class BackendOperatingSystemsResource
        extends AbstractBackendCollectionResource<OperatingSystemInfo, Integer>
        implements OperatingSystemsResource {
    public BackendOperatingSystemsResource() {
        super(OperatingSystemInfo.class, Integer.class);
    }

    @Override
    public OperatingSystemInfos list() {
        OsRepository repository = SimpleDependencyInjector.getInstance().get(OsRepository.class);
        final Map<Integer, VmIconIdSizePair> iconDefaults = getIconDefaults();
        List<Integer> ids = repository.getOsIds();
        Map<Integer, String> uniqueNames = repository.getUniqueOsNames();
        Map<Integer, String> names = repository.getOsNames();
        OperatingSystemInfos collection = new OperatingSystemInfos();
        for (Integer id : ids) {
            OperatingSystemInfo model = new OperatingSystemInfo();
            model.setId(id.toString());
            if (iconDefaults.containsKey(id)) {
                final VmIconIdSizePair iconDefault = iconDefaults.get(id);
                model.setSmallIcon(IconHelper.createIcon(iconDefault.getSmall()));
                model.setLargeIcon(IconHelper.createIcon(iconDefault.getLarge()));
            }
            String uniqueName = uniqueNames.get(id);
            if (uniqueName != null) {
                model.setName(uniqueName);
            }
            String name = names.get(id);
            if (name != null) {
                model.setDescription(name);
            }
            model.setArchitecture(CPUMapper.map(repository.getArchitectureFromOS(id), null));
            collection.getOperatingSystemInfos().add(addLinks(model));
        }
        return collection;
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, VmIconIdSizePair> getIconDefaults() {
        return (Map<Integer, VmIconIdSizePair>) getEntity(
                Map.class, QueryType.GetVmIconDefaults, new QueryParametersBase(), "Icon defaults");
    }

    @Override
    public OperatingSystemResource getOperatingSystemResource(String id) {
        return inject(new BackendOperatingSystemResource(id));
    }
}
