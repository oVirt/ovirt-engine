package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.Set;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.BooleanUtils;
import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.resource.VmDiskResource;
import org.ovirt.engine.api.resource.VmDisksResource;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;
import org.ovirt.engine.api.restapi.resource.utils.DiskResourceUtils;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.api.utils.LinkHelper;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmDisksResource
        extends AbstractBackendDevicesResource<Disk, Disks, org.ovirt.engine.core.common.businessentities.storage.Disk>
        implements VmDisksResource {

    static final String[] SUB_COLLECTIONS = {"permissions", "statistics"};

    public BackendVmDisksResource(Guid parentId,
                                  VdcQueryType queryType,
                                  VdcQueryParametersBase queryParams) {
        super(Disk.class,
              Disks.class,
              org.ovirt.engine.core.common.businessentities.storage.Disk.class,
              parentId,
              queryType,
              queryParams,
              VdcActionType.AddDisk,
              VdcActionType.RemoveDisk,
              VdcActionType.UpdateVmDisk,
              SUB_COLLECTIONS);
    }

    @Override
    public Response add(Disk disk) {
        validateEnums(Disk.class, disk);

        if (disk.isSetId()) {
            return Response.fromResponse(attachDiskToVm(disk))
                           .entity(map(lookupEntity(asGuid(disk.getId()))))
                           .build();
        }else {
            validateDiskForCreation(disk);
            org.ovirt.engine.core.common.businessentities.StorageDomain storageDomain = getStorageDomainById(getStorageDomainId(disk));
            if (storageDomain != null) {
                disk.setStorageType(DiskMapper.map(storageDomain.getStorageDomainType()).value());
            }
            return performCreate(addAction,
                    getAddParameters(map(disk), disk),
                    getEntityIdResolver(disk.getName()));
        }
    }

    @Override
    public Response remove(String id, Action action) {
        getEntity(id); //verifies that entity exists, returns 404 otherwise.
        if (action.isSetDetach() && action.isDetach()) {
            return performAction(VdcActionType.DetachDiskFromVm,
                    new AttachDetachVmDiskParameters(parentId, Guid.createGuidFromStringDefaultEmpty(id)));
        } else {
            return remove(id);
        }
    }

    @Override
    @SingleEntityResource
    public VmDiskResource getDeviceSubResource(String id) {
        return inject(new BackendVmDiskResource(id,
                                              this,
                                              updateType,
                                              getUpdateParametersProvider(),
                                              getRequiredUpdateFields(),
                                              subCollections));
    }

    @Override
    protected <T> boolean matchEntity(org.ovirt.engine.core.common.businessentities.storage.Disk entity, T id) {
        return id != null && (id.equals(entity.getId()));
    }

    @Override
    protected boolean matchEntity(org.ovirt.engine.core.common.businessentities.storage.Disk entity, String name) {
        return false;
    }

    @Override
    protected String[] getRequiredAddFields() {
        return new String[] { "provisionedSize|size", "format", "interface" };
    }

    @Override
    protected String[] getRequiredUpdateFields() {
        return new String[0];
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
            VM vm = new VM();
            vm.setId(snapshot.getVmId().toString());
            snapshotInfo.setVm(vm);
            model.setSnapshot(snapshotInfo);
            LinkHelper.addLinks(getUriInfo(), snapshotInfo, null, false);
            model.setSnapshot(snapshotInfo);
        }

        return model;
    }

    @Override
    protected VdcActionParametersBase getAddParameters(org.ovirt.engine.core.common.businessentities.storage.Disk entity, Disk disk) {
        AddDiskParameters parameters = new AddDiskParameters(parentId, entity);
        parameters.setStorageDomainId(getStorageDomainId(disk));
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
    protected VdcActionParametersBase getRemoveParameters(String id) {
        return new RemoveDiskParameters(asGuid(id));
    }

    @Override
    protected ParametersProvider<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk> getUpdateParametersProvider() {
        return new UpdateParametersProvider();
    }

    protected class UpdateParametersProvider implements ParametersProvider<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk> {
        @Override
        public VdcActionParametersBase getParameters(Disk incoming, org.ovirt.engine.core.common.businessentities.storage.Disk entity) {
            return new UpdateVmDiskParameters(parentId, entity.getId(), map(incoming, entity));
        }
    }

    @Override
    protected Disk doPopulate(Disk model, org.ovirt.engine.core.common.businessentities.storage.Disk entity) {
        return model;
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
        boolean isDiskActive = BooleanUtils.toBooleanDefaultIfNull(disk.isActive(), false);
        boolean isDiskReadOnly = BooleanUtils.toBooleanDefaultIfNull(disk.isReadOnly(), false);
        AttachDetachVmDiskParameters params = new AttachDetachVmDiskParameters(parentId,
                Guid.createGuidFromStringDefaultEmpty(disk.getId()), isDiskActive, isDiskReadOnly);

        if (disk.isSetSnapshot()) {
            validateParameters(disk, "snapshot.id");
            params.setSnapshotId(asGuid(disk.getSnapshot().getId()));
        }

        return performAction(VdcActionType.AttachDiskToVm, params);
    }

    protected void validateDiskForCreation(Disk disk) {
        validateParameters(disk, 3, "interface");
        if (DiskResourceUtils.isLunDisk(disk)) {
            validateParameters(disk.getLunStorage(), 3, "type"); // when creating a LUN disk, user must specify type.
            StorageType storageType = StorageType.fromValue(disk.getLunStorage().getType());
            if (storageType != null && storageType == StorageType.ISCSI) {
                validateParameters(disk.getLunStorage().getLogicalUnits().get(0), 3, "address", "target", "port", "id");
            }
        } else if (disk.isSetLunStorage() && disk.getLunStorage().getLogicalUnits().isEmpty()) {
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
}
