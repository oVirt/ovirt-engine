package org.ovirt.engine.api.restapi.resource;

import java.util.Map;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.Vms;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.StorageDomainContentDisksResource;
import org.ovirt.engine.api.resource.StorageDomainVmResource;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.RemoveVmFromImportExportParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainVmResource
    extends AbstractBackendStorageDomainContentResource<Vms, Vm, org.ovirt.engine.core.common.businessentities.VM>
    implements StorageDomainVmResource {

    public static final String COLLAPSE_SNAPSHOTS = "collapse_snapshots";

    org.ovirt.engine.core.common.businessentities.VM vm;

    public BackendStorageDomainVmResource(BackendStorageDomainVmsResource parent, String vmId) {
        super(vmId, parent, Vm.class, org.ovirt.engine.core.common.businessentities.VM.class, "disks");
    }

    @Override
    public Vm get() {
        switch (parent.getStorageDomainType()) {
        case Data:
        case Master:
            return getFromDataDomain();
        case ImportExport:
            return getFromExportDomain();
        case ISO:
        case Unknown:
        default:
            return null;
        }
    }

    private Vm getFromDataDomain() {
        return performGet(VdcQueryType.GetVmByVmId, new IdQueryParameters(guid));
    }

    private Vm getFromExportDomain() {
        org.ovirt.engine.core.common.businessentities.VM entity = getEntity();
        return addLinks(populate(map(entity, null), entity), null, new String[0]);
    }

    @Override
    public Response register(Action action) {
        validateParameters(action, "cluster.id|name");
        ImportVmParameters params = new ImportVmParameters();
        params.setContainerId(guid);
        params.setStorageDomainId(parent.getStorageDomainId());
        params.setClusterId(getClusterId(action));
        params.setImagesExistOnTargetStorageDomain(true);

        if (action.isSetClone()) {
            params.setImportAsNewEntity(action.isClone());
            if (action.isSetVm() && action.getVm().isSetName()) {
                params.getVm().setName(action.getVm().getName());
            }
        }

        return doAction(VdcActionType.ImportVmFromConfiguration, params, action);
    }

    @Override
    public Response doImport(Action action) {
        validateParameters(action, "cluster.id|name", "storageDomain.id|name");
        Guid destStorageDomainId = getDestStorageDomainId(action);

        ImportVmParameters params = new ImportVmParameters(getEntity(),
                parent.getStorageDomainId(),
                destStorageDomainId,
                parent.getDataCenterId(destStorageDomainId),
                getClusterId(action));
        params.setImageToDestinationDomainMap(getDiskToDestinationMap(action));
        params.setForceOverride(action.isSetExclusive() ? action.isExclusive() : false);

        boolean collapseSnapshots = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, COLLAPSE_SNAPSHOTS, true, false);
        if (collapseSnapshots) {
            params.setCopyCollapse(collapseSnapshots);
        }

        setVolumesTypeFormat(action);

        if (action.isSetClone()) {
            params.setImportAsNewEntity(action.isClone());
            if (action.isSetVm() && action.getVm().isSetName()) {
                params.getVm().setName(action.getVm().getName());
            }
        }

        return doAction(VdcActionType.ImportVm, params, action);
    }

    private void setVolumesTypeFormat(Action action) {
        if (action.isSetVm()) {
            Vm modelVm = action.getVm();
            if (!modelVm.isSetDiskAttachments()) {
                return;
            }
            Map<Guid, org.ovirt.engine.core.common.businessentities.storage.Disk> entityDisks = getDiskMap();
            for (DiskAttachment modelDiskAttachment : modelVm.getDiskAttachments().getDiskAttachments()) {
                Disk modelDisk = modelDiskAttachment.getDisk();
                if (modelDisk != null) {
                    validateParameters(modelDisk, "id");
                    Guid modelDiskId = Guid.createGuidFromString(modelDisk.getId());
                    DiskImage entityDisk = (DiskImage) entityDisks.get(modelDiskId);
                    if (entityDisk == null) {
                        continue;
                    }
                    if (modelDisk.isSetFormat()) {
                        VolumeFormat entityDiskFormat = DiskMapper.map(modelDisk.getFormat(), null);
                        entityDisk.setVolumeFormat(entityDiskFormat);
                    }
                    if (modelDisk.isSetSparse()) {
                        entityDisk.setVolumeType(modelDisk.isSparse() ? VolumeType.Sparse : VolumeType.Preallocated);
                    }
                }
            }
        }
    }

    @Override
    public ActionResource getActionResource(String action, String ids) {
        return inject(new BackendActionResource(action, ids));
    }

    @Override
    public StorageDomainContentDisksResource getDisksResource() {
        return inject(new BackendExportDomainDisksResource(this));
    }

    @Override
    protected Vm addParents(Vm vm) {
        vm.setStorageDomain(parent.getStorageDomainModel());
        return vm;
    }

    protected org.ovirt.engine.core.common.businessentities.VM getEntity() {
        if (vm != null) {
            return vm;
        }
        for (org.ovirt.engine.core.common.businessentities.VM entity : parent.getEntitiesFromExportDomain()) {
            if (guid.equals(entity.getId())) {
                vm = entity;
                return entity;
            }
        }
        return entityNotFound();
    }

    @Override
    public Map<Guid, org.ovirt.engine.core.common.businessentities.storage.Disk> getDiskMap() {
        return getEntity().getDiskMap();
    }

    @Override
    public Response remove() {
        get();
        RemoveVmFromImportExportParameters params = new RemoveVmFromImportExportParameters(
                guid,
                parent.storageDomainId,
                getDataCenterId(parent.storageDomainId));
        return performAction(VdcActionType.RemoveVmFromImportExport, params);
    }

}
