/*
Copyright (c) 2015 Red Hat, Inc.

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

package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.DiskInterface;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.VmDiskResource;
import org.ovirt.engine.api.resource.VmDisksResource;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmDisksResource
        extends AbstractBackendCollectionResource<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk>
        implements VmDisksResource {

    static final String[] SUB_COLLECTIONS = {"permissions", "statistics" };

    private Guid vmId;

    public BackendVmDisksResource(Guid vmId) {
        super(Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class, SUB_COLLECTIONS);
        this.vmId = vmId;
    }

    public Disks list() {
        return mapCollection(getBackendCollection(VdcQueryType.GetAllDisksByVmId, new IdQueryParameters(vmId)));
    }

    private Disks mapCollection(List<org.ovirt.engine.core.common.businessentities.storage.Disk> entities) {
        Disks collection = new Disks();
        for (org.ovirt.engine.core.common.businessentities.storage.Disk entity : entities) {
            collection.getDisks().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    @Override
    public Response add(Disk disk) {
        DiskAttachment diskAttachment = new DiskAttachment();

        if (disk.isSetBootable()) {
            diskAttachment.setBootable(disk.isBootable());
        }
        else {
            diskAttachment.setBootable(false);
        }

        if (disk.isSetInterface()) {
            diskAttachment.setInterface(disk.getInterface());
        }
        else {
            diskAttachment.setInterface(DiskInterface.VIRTIO);
        }

        if (disk.isSetActive()) {
            diskAttachment.setActive(disk.isActive());
        }

        diskAttachment.setDisk(disk);
        if (disk.isSetId()) {
            Guid diskId = Guid.createGuidFromStringDefaultEmpty(disk.getId());
            return getAttachmentsResource().attachDiskToVm(this, diskAttachment, new OldAttachDiskResolver(diskId));
        }
        else {
            return getAttachmentsResource().createDisk(this, diskAttachment, new OldAddDiskResolver());
        }
    }

    @Override
    public VmDiskResource getDiskResource(String id) {
        return inject(new BackendVmDiskResource(id, vmId));
    }

    public BackendDiskAttachmentsResource getAttachmentsResource() {
        return inject(new BackendDiskAttachmentsResource(vmId));
    }


    @Override
    protected Disk addLinks(Disk model, String... subCollectionMembersToExclude) {
        Snapshot snapshotInfo = model.getSnapshot();
        model.setSnapshot(null);
        super.addLinks(model, subCollectionMembersToExclude);
        if (snapshotInfo != null) {
            org.ovirt.engine.core.common.businessentities.Snapshot snapshot =
                    getEntity(org.ovirt.engine.core.common.businessentities.Snapshot.class,
                            VdcQueryType.GetSnapshotBySnapshotId,
                            new IdQueryParameters(asGuid(snapshotInfo.getId())),
                            snapshotInfo.getId());
            Vm vm = new Vm();
            vm.setId(snapshot.getVmId().toString());
            snapshotInfo.setVm(vm);
            model.setSnapshot(snapshotInfo);
            LinkHelper.addLinks(snapshotInfo, null, false);
            model.setSnapshot(snapshotInfo);
        }

        return model;
    }

    private org.ovirt.engine.core.common.businessentities.StorageDomain getStorageDomainById(Guid id) {
        return getEntity(org.ovirt.engine.core.common.businessentities.StorageDomain.class, VdcQueryType.GetStorageDomainById, new IdQueryParameters(id), id.toString());
    }

    @Override
    protected Disk deprecatedPopulate(Disk model, org.ovirt.engine.core.common.businessentities.storage.Disk entity) {
        Set<String> details = DetailHelper.getDetails(httpHeaders, uriInfo);
        if (details.contains("statistics")) {
            addStatistics(model, entity);
        }
        return model;
    }

    private void addStatistics(Disk model, org.ovirt.engine.core.common.businessentities.storage.Disk entity) {
        model.setStatistics(new Statistics());
        DiskStatisticalQuery query = new DiskStatisticalQuery(newModel(model.getId()));
        List<Statistic> statistics = query.getStatistics(entity);
        for (Statistic statistic : statistics) {
            LinkHelper.addLinks(statistic, query.getParentType());
        }
        model.getStatistics().getStatistics().addAll(statistics);
    }

    @Override
    protected Disk addParents(Disk disk) {
        Vm vm = new Vm();
        vm.setId(vmId.toString());
        disk.setVm(vm);
        return disk;
    }

    // The command that adds a disk returns the identifier of the new disk, so we can use simple resolver in that case.
    class OldAddDiskResolver implements IResolver<Guid, org.ovirt.engine.core.common.businessentities.storage.Disk> {
        @Override
        public org.ovirt.engine.core.common.businessentities.storage.Disk resolve(Guid id) throws BackendFailureException {
            return getEntity(
                org.ovirt.engine.core.common.businessentities.storage.Disk.class,
                VdcQueryType.GetDiskByDiskId,
                new IdQueryParameters(id),
                id.toString(),
                true
            );
        }
    }

    // The command that attaches a disk doesn't return the disk id, so we need to pass it to the resolver:
    private class OldAttachDiskResolver implements IResolver<Guid, org.ovirt.engine.core.common.businessentities.storage.Disk> {
        private Guid diskId;
        public OldAttachDiskResolver(Guid diskId) {
            this.diskId = diskId;
        }

        @Override
        public org.ovirt.engine.core.common.businessentities.storage.Disk resolve(Guid id) throws BackendFailureException {
            return getEntity(
                    org.ovirt.engine.core.common.businessentities.storage.Disk.class,
                    VdcQueryType.GetDiskByDiskId,
                    new IdQueryParameters(diskId),
                    diskId.toString(),
                    true
            );
        }
    }
}
