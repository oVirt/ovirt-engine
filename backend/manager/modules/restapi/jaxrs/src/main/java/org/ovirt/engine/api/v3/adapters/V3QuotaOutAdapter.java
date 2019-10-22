/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
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
