/*
Copyright (c) 2016 Red Hat, Inc.

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

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.IscsiBond;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3IscsiBond;
import org.ovirt.engine.api.v3.types.V3Networks;
import org.ovirt.engine.api.v3.types.V3StorageConnections;

public class V3IscsiBondOutAdapter implements V3Adapter<IscsiBond, V3IscsiBond> {
    @Override
    public V3IscsiBond adapt(IscsiBond from) {
        V3IscsiBond to = new V3IscsiBond();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDataCenter()) {
            to.setDataCenter(adaptOut(from.getDataCenter()));
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetNetworks()) {
            to.setNetworks(new V3Networks());
            to.getNetworks().getNetworks().addAll(adaptOut(from.getNetworks().getNetworks()));
        }
        if (from.isSetStorageConnections()) {
            to.setStorageConnections(new V3StorageConnections());
            to.getStorageConnections().getStorageConnections().addAll(adaptOut(from.getStorageConnections().getStorageConnections()));
        }
        return to;
    }
}
