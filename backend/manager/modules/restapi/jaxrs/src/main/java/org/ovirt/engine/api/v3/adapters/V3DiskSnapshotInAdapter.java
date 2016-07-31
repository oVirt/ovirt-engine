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

import org.ovirt.engine.api.model.DiskFormat;
import org.ovirt.engine.api.model.DiskSnapshot;
import org.ovirt.engine.api.model.DiskStatus;
import org.ovirt.engine.api.model.DiskStorageType;
import org.ovirt.engine.api.model.ScsiGenericIO;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.model.Vms;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3DiskSnapshot;

public class V3DiskSnapshotInAdapter implements V3Adapter<V3DiskSnapshot, DiskSnapshot> {
    @Override
    public DiskSnapshot adapt(V3DiskSnapshot from) {
        DiskSnapshot to = new DiskSnapshot();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetActualSize()) {
            to.setActualSize(from.getActualSize());
        }
        if (from.isSetAlias()) {
            to.setAlias(from.getAlias());
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetDisk()) {
            to.setDisk(adaptIn(from.getDisk()));
        }
        if (from.isSetDiskProfile()) {
            to.setDiskProfile(adaptIn(from.getDiskProfile()));
        }
        if (from.isSetFormat()) {
            to.setFormat(DiskFormat.fromValue(from.getFormat()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetImageId()) {
            to.setImageId(from.getImageId());
        }
        if (from.isSetInstanceType()) {
            to.setInstanceType(adaptIn(from.getInstanceType()));
        }
        if (from.isSetLogicalName()) {
            to.setLogicalName(from.getLogicalName());
        }
        if (from.isSetLunStorage()) {
            to.setLunStorage(adaptIn(from.getLunStorage()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetOpenstackVolumeType()) {
            to.setOpenstackVolumeType(adaptIn(from.getOpenstackVolumeType()));
        }
        if (from.isSetPropagateErrors()) {
            to.setPropagateErrors(from.isPropagateErrors());
        }
        if (from.isSetProvisionedSize()) {
            to.setProvisionedSize(from.getProvisionedSize());
        }
        if (from.isSetQuota()) {
            to.setQuota(adaptIn(from.getQuota()));
        }
        if (from.isSetReadOnly()) {
            to.setReadOnly(from.isReadOnly());
        }
        if (from.isSetSgio()) {
            to.setSgio(ScsiGenericIO.fromValue(from.getSgio()));
        }
        if (from.isSetShareable()) {
            to.setShareable(from.isShareable());
        }
        if (from.isSetSnapshot()) {
            to.setSnapshot(adaptIn(from.getSnapshot()));
        }
        if (from.isSetSparse()) {
            to.setSparse(from.isSparse());
        }
        if (from.isSetStatistics()) {
            to.setStatistics(new Statistics());
            to.getStatistics().getStatistics().addAll(adaptIn(from.getStatistics().getStatistics()));
        }
        if (from.isSetStatus() && from.getStatus().isSetState()) {
            to.setStatus(DiskStatus.fromValue(from.getStatus().getState()));
        }
        if (from.isSetStorageDomain()) {
            to.setStorageDomain(adaptIn(from.getStorageDomain()));
        }
        if (from.isSetStorageDomains()) {
            to.setStorageDomains(new StorageDomains());
            to.getStorageDomains().getStorageDomains().addAll(adaptIn(from.getStorageDomains().getStorageDomains()));
        }
        if (from.isSetStorageType()) {
            to.setStorageType(DiskStorageType.fromValue(from.getStorageType()));
        }
        if (from.isSetTemplate()) {
            to.setTemplate(adaptIn(from.getTemplate()));
        }
        if (from.isSetUsesScsiReservation()) {
            to.setUsesScsiReservation(from.isUsesScsiReservation());
        }
        if (from.isSetVm()) {
            to.setVm(adaptIn(from.getVm()));
        }
        if (from.isSetVms()) {
            to.setVms(new Vms());
            to.getVms().getVms().addAll(adaptIn(from.getVms().getVMs()));
        }
        if (from.isSetWipeAfterDelete()) {
            to.setWipeAfterDelete(from.isWipeAfterDelete());
        }
        return to;
    }
}
