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

import org.ovirt.engine.api.model.LogicalUnits;
import org.ovirt.engine.api.model.VolumeGroup;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VolumeGroup;

public class V3VolumeGroupInAdapter implements V3Adapter<V3VolumeGroup, VolumeGroup> {
    @Override
    public VolumeGroup adapt(V3VolumeGroup from) {
        VolumeGroup to = new VolumeGroup();
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetLogicalUnits()) {
            to.setLogicalUnits(new LogicalUnits());
            to.getLogicalUnits().getLogicalUnits().addAll(adaptIn(from.getLogicalUnits()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        return to;
    }
}
