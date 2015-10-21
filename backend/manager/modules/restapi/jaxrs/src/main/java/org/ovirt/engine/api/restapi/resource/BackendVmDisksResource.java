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

import org.apache.commons.lang.BooleanUtils;
import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.VmDiskResource;
import org.ovirt.engine.api.resource.VmDisksResource;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.resource.utils.DiskResourceUtils;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.api.utils.LinkHelper;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmDisksResource
        extends AbstractBackendCollectionResource<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk>
        implements VmDisksResource {

    static final String[] SUB_COLLECTIONS = {"permissions", "statistics"};

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
        validateEnums(Disk.class, disk);

        if (disk.isSetId()) {
            return attachDiskToVm(disk);
        }
        else {
            validateDiskForCreation(disk);
            updateStorageTypeForDisk(disk);
            return performCreate(
                VdcActionType.AddDisk,
                getAddParameters(map(disk), disk),
                new AddDiskResolver()
            );
        }
    }

    protected void updateStorageTypeForDisk(Disk disk) {
        Guid storageDomainId = getStorageDomainId(disk);
        if (storageDomainId != null) {
            org.ovirt.engine.core.common.businessentities.StorageDomain storageDomain = getStorageDomainById(storageDomainId);
            if (storageDomain != null) {
                disk.setStorageType(DiskMapper.map(storageDomain.getStorageDomainType()).value());
            }
        }
    }

    @Override
    public VmDiskResource getDiskResource(String id) {
        return inject(new BackendVmDiskResource(id, vmId));
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
            LinkHelper.addLinks(getUriInfo(), snapshotInfo, null, false);
            model.setSnapshot(snapshotInfo);
        }

        return model;
    }

    private VdcActionParametersBase getAddParameters(org.ovirt.engine.core.common.businessentities.storage.Disk entity, Disk disk) {
        AddDiskParameters parameters = new AddDiskParameters(vmId, entity);
        Guid storageDomainId = getStorageDomainId(disk);
        if (storageDomainId != null) {
            parameters.setStorageDomainId(storageDomainId);
        }
        if (disk.isSetActive()) {
            parameters.setPlugDiskToVm(disk.isActive());
        }
        if (disk.isSetLunStorage() && disk.getLunStorage().isSetHost()) {
            parameters.setVdsId(getHostId(disk.getLunStorage().getHost()));
        }
        return parameters;
    }

    private Guid getStorageDomainId(Disk disk) {
        if (disk.isSetStorageDomains() && disk.getStorageDomains().isSetStorageDomains()
                && disk.getStorageDomains().getStorageDomains().get(0).isSetId()) {
            return asGuid(disk.getStorageDomains().getStorageDomains().get(0).getId());
        } else if (disk.isSetStorageDomains() && disk.getStorageDomains().getStorageDomains().get(0).isSetName()) {
            Guid storageDomainId = getStorageDomainIdByName(disk.getStorageDomains().getStorageDomains().get(0).getName());
            if (storageDomainId == null) {
                notFound(StorageDomain.class);
            } else {
                return storageDomainId;
            }
        }
        return null;
    }

    private Guid getStorageDomainIdByName(String storageDomainName) {
        List<org.ovirt.engine.core.common.businessentities.StorageDomain> storageDomains =
                getBackendCollection(org.ovirt.engine.core.common.businessentities.StorageDomain.class,
                        VdcQueryType.GetAllStorageDomains,
                        new VdcQueryParametersBase());
        for (org.ovirt.engine.core.common.businessentities.StorageDomain storageDomain : storageDomains) {
            if (storageDomain.getStorageName().equals(storageDomainName)) {
                return storageDomain.getId();
            }
        }
        return null;
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
            LinkHelper.addLinks(uriInfo, statistic, query.getParentType());
        }
        model.getStatistics().getStatistics().addAll(statistics);
    }

    private Response attachDiskToVm(Disk disk) {
        Guid diskId = Guid.createGuidFromStringDefaultEmpty(disk.getId());
        boolean isDiskActive = BooleanUtils.toBooleanDefaultIfNull(disk.isActive(), false);
        boolean isDiskReadOnly = BooleanUtils.toBooleanDefaultIfNull(disk.isReadOnly(), false);
        AttachDetachVmDiskParameters params = new AttachDetachVmDiskParameters(vmId, diskId, isDiskActive,
                isDiskReadOnly);

        if (disk.isSetSnapshot()) {
            validateParameters(disk, "snapshot.id");
            params.setSnapshotId(asGuid(disk.getSnapshot().getId()));
        }

        return performCreate(VdcActionType.AttachDiskToVm, params, new AttachDiskResolver(diskId));
    }

    protected void validateDiskForCreation(Disk disk) {
        validateParameters(disk, 3, "interface");
        if (DiskResourceUtils.isLunDisk(disk)) {
            validateParameters(disk.getLunStorage(), 3, "type"); // when creating a LUN disk, user must specify type.
            StorageType storageType = StorageType.fromValue(disk.getLunStorage().getType());
            if (storageType != null && storageType == StorageType.ISCSI) {
                validateParameters(disk.getLunStorage().getLogicalUnits().getLogicalUnits().get(0), 3, "address", "target", "port", "id");
            }
        } else if (disk.isSetLunStorage() && (!disk.getLunStorage().isSetLogicalUnits() || !disk.getLunStorage().getLogicalUnits().isSetLogicalUnits())) {
            // TODO: Implement nested entity existence validation infra for validateParameters()
            throw new WebFaultException(null,
                                        localize(Messages.INCOMPLETE_PARAMS_REASON),
                                        localize(Messages.INCOMPLETE_PARAMS_DETAIL_TEMPLATE, "LogicalUnit", "", "add"),
                                        Response.Status.BAD_REQUEST);
        } else {
            validateParameters(disk, 3, "provisionedSize|size", "format"); // Non lun disks require size and format
        }
        validateEnums(Disk.class, disk);
    }

    @Override
    protected Disk addParents(Disk disk) {
        Vm vm = new Vm();
        vm.setId(vmId.toString());
        disk.setVm(vm);
        return disk;
    }

    // The command that adds a disk returns the identifier of the new disk, so we can use simple resolver in that case.
    private class AddDiskResolver implements IResolver<Guid, org.ovirt.engine.core.common.businessentities.storage.Disk> {
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

    // The command that attaches a disk doesn't resturn the disk id, so we need to pass it to the resolver:
    private class AttachDiskResolver implements IResolver<Guid, org.ovirt.engine.core.common.businessentities.storage.Disk> {
        private Guid diskId;

        public AttachDiskResolver(Guid diskId) {
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
