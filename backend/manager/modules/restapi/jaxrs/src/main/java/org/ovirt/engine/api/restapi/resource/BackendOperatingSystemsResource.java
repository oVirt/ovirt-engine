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

import java.util.List;
import java.util.Map;

import org.ovirt.engine.api.model.OperatingSystemInfo;
import org.ovirt.engine.api.model.OperatingSystemInfos;
import org.ovirt.engine.api.resource.OperatingSystemResource;
import org.ovirt.engine.api.resource.OperatingSystemsResource;
import org.ovirt.engine.api.restapi.util.IconHelper;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
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
            collection.getOperatingSystemInfos().add(addLinks(model));
        }
        return collection;
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, VmIconIdSizePair> getIconDefaults() {
        return (Map<Integer, VmIconIdSizePair>) getEntity(
                Map.class, VdcQueryType.GetVmIconDefaults, new VdcQueryParametersBase(), "Icon defaults");
    }

    @Override
    public OperatingSystemResource getOperatingSystemResource(String id) {
        return inject(new BackendOperatingSystemResource(id));
    }
}
