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

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.IscsiBond;
import org.ovirt.engine.api.model.Networks;
import org.ovirt.engine.api.model.StorageConnections;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3IscsiBond;

public class V3IscsiBondInAdapter implements V3Adapter<V3IscsiBond, IscsiBond> {
    @Override
    public IscsiBond adapt(V3IscsiBond from) {
        IscsiBond to = new IscsiBond();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDataCenter()) {
            to.setDataCenter(adaptIn(from.getDataCenter()));
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
            to.setNetworks(new Networks());
            to.getNetworks().getNetworks().addAll(adaptIn(from.getNetworks().getNetworks()));
        }
        if (from.isSetStorageConnections()) {
            to.setStorageConnections(new StorageConnections());
            to.getStorageConnections().getStorageConnections().addAll(adaptIn(from.getStorageConnections().getStorageConnections()));
        }
        return to;
    }
}
