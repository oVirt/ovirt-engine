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

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.api.model.OperatingSystemInfo;
import org.ovirt.engine.api.model.OperatingSystemInfos;
import org.ovirt.engine.api.resource.OperatingSystemResource;
import org.ovirt.engine.api.resource.OperatingSystemsResource;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;


public class BackendOperatingSystemsResource
        extends AbstractBackendCollectionResource<OperatingSystemInfo, Integer>
        implements OperatingSystemsResource {
    public BackendOperatingSystemsResource() {
        super(OperatingSystemInfo.class, Integer.class);
    }

    @Override
    public OperatingSystemInfos list() {
        OsRepository repository = SimpleDependecyInjector.getInstance().get(OsRepository.class);
        ArrayList<Integer> ids = repository.getOsIds();
        HashMap<Integer, String> uniqueNames = repository.getUniqueOsNames();
        HashMap<Integer, String> names = repository.getOsNames();
        OperatingSystemInfos collection = new OperatingSystemInfos();
        for (Integer id : ids) {
            OperatingSystemInfo model = new OperatingSystemInfo();
            model.setId(id.toString());
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

    @Override
    @SingleEntityResource
    public OperatingSystemResource getOperatingSystem(String id) {
        return inject(new BackendOperatingSystemResource(id));
    }

    @Override
    protected OperatingSystemInfo doPopulate(OperatingSystemInfo model, Integer entity) {
        return model;
    }

    @Override
    protected Response performRemove(String id) {
        throw new NotImplementedException();
    }
}
