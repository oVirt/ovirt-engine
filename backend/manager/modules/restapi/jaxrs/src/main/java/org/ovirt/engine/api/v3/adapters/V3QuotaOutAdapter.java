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

import org.ovirt.engine.api.model.Quota;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Disks;
import org.ovirt.engine.api.v3.types.V3Quota;
import org.ovirt.engine.api.v3.types.V3Users;
import org.ovirt.engine.api.v3.types.V3VMs;

public class V3QuotaOutAdapter implements V3Adapter<Quota, V3Quota> {
    @Override
    public V3Quota adapt(Quota from) {
        V3Quota to = new V3Quota();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetClusterHardLimitPct()) {
            to.setClusterHardLimitPct(from.getClusterHardLimitPct());
        }
        if (from.isSetClusterSoftLimitPct()) {
            to.setClusterSoftLimitPct(from.getClusterSoftLimitPct());
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
        if (from.isSetDisks()) {
            to.setDisks(new V3Disks());
            to.getDisks().getDisks().addAll(adaptOut(from.getDisks().getDisks()));
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
        if (from.isSetStorageHardLimitPct()) {
            to.setStorageHardLimitPct(from.getStorageHardLimitPct());
        }
        if (from.isSetStorageSoftLimitPct()) {
            to.setStorageSoftLimitPct(from.getStorageSoftLimitPct());
        }
        if (from.isSetUsers()) {
            to.setUsers(new V3Users());
            to.getUsers().getUsers().addAll(adaptOut(from.getUsers().getUsers()));
        }
        if (from.isSetVms()) {
            to.setVms(new V3VMs());
            to.getVms().getVMs().addAll(adaptOut(from.getVms().getVms()));
        }
        return to;
    }
}
