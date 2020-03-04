package org.ovirt.engine.api.restapi.resource;

import java.util.Collection;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.RegistrationAffinityGroupMapping;
import org.ovirt.engine.api.model.RegistrationAffinityLabelMapping;
import org.ovirt.engine.api.model.RegistrationClusterMapping;
import org.ovirt.engine.api.model.RegistrationDomainMapping;
import org.ovirt.engine.api.model.RegistrationLunMapping;
import org.ovirt.engine.api.model.RegistrationRoleMapping;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.Vms;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.StorageDomainContentDisksResource;
import org.ovirt.engine.api.resource.StorageDomainVmDiskAttachmentsResource;
import org.ovirt.engine.api.resource.StorageDomainVmResource;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.api.restapi.types.ExternalRegistrationConfigurationMapper;
import org.ovirt.engine.api.restapi.types.ExternalVnicProfileMappingMapper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportVmFromConfParameters;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.RemoveUnregisteredEntityParameters;
import org.ovirt.engine.core.common.action.RemoveVmFromImportExportParameters;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.queries.GetUnregisteredEntityQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainVmResource
    extends AbstractBackendStorageDomainContentResource<Vms, Vm, org.ovirt.engine.core.common.businessentities.VM>
    implements StorageDomainVmResource {

    public static final String COLLAPSE_SNAPSHOTS = "collapse_snapshots";

    org.ovirt.engine.core.common.businessentities.VM vm;

    public BackendStorageDomainVmResource(BackendStorageDomainVmsResource parent, String vmId) {
        super(vmId, parent, Vm.class, org.ovirt.engine.core.common.businessentities.VM.class);
    }

    @Override
    public Vm get() {
        switch (parent.getStorageDomainType()) {
        case Data:
        case Master:
            return getFromDataDomain();
        case ImportExport:
            return getFromExportDomain();
        default:
            return null;
        }
    }

    private Vm getFromDataDomain() {
        return performGet(QueryType.GetVmByVmId, new IdQueryParameters(guid));
    }

    private Vm getFromExportDomain() {
        org.ovirt.engine.core.common.businessentities.VM entity = getEntity();
        return addLinks(populate(map(entity, null), entity), null, new String[0]);
    }

    @Override
    public Response register(Action action) {
        ImportVmFromConfParameters params = new ImportVmFromConfParameters();
        if (action.isSetRegistrationConfiguration()) {
            validateClusterMappings(action);
            validateRoleMappings(action);
            validateDomainMappings(action);
            validateAffinityGroupMappings(action);
            validateAffinityLabelMappings(action);
            validateLunMappings(action);
        }
        if (BackendVnicProfileHelper.foundOnlyDeprecatedVnicProfileMapping(action)) {
            // This code block is for backward compatibility with {@link VnicProfileMapping}s that are specified
            // outside the registration_configuration code. This specification is deprecated since 4.2.1 .
            // When these mappings are removed from the ovirt-engine-api-model, this whole code block can be removed
            // as well. In the meantime, if there are {@link VnicProfileMapping}s outside the registration_configuration
            // block and no {@link RegistrationVnicProfileMapping}s inside it, they will be processed and used.
            BackendVnicProfileHelper.validateVnicMappings(this, action);
            Collection<ExternalVnicProfileMapping> vnicProfileMappings = ExternalVnicProfileMappingMapper.mapFromModel(
                    action.getVnicProfileMappings());
            params.setExternalVnicProfileMappings(vnicProfileMappings);
        }

        ExternalRegistrationConfigurationMapper.mapFromModel(action.getRegistrationConfiguration(), params);
        params.setReassignBadMacs(getReassignBadMacs(action));
        params.setContainerId(guid);
        params.setStorageDomainId(parent.getStorageDomainId());
        if (action.isSetCluster()) {
            params.setClusterId(getClusterId(action));
        }
        params.setImagesExistOnTargetStorageDomain(true);

        if (action.isSetClone()) {
            params.setImportAsNewEntity(action.isClone());
            if (action.isSetVm() && action.getVm().isSetName()) {
                params.getVm().setName(action.getVm().getName());
            }
        }
        if (action.isSetAllowPartialImport()) {
            params.setAllowPartialImport(action.isAllowPartialImport());
        }
        if (action.isSetName()) {
            params.setName(action.getName());
        }
        return doAction(ActionType.ImportVmFromConfiguration, params, action);
    }

    @Override
    public Response doImport(Action action) {
        validateParameters(action, "storageDomain.id|name");
        Guid destStorageDomainId = getDestStorageDomainId(action);
        Guid clusterId = null;
        if (action.isSetCluster()) {
            clusterId = getClusterId(action);
        }

        ImportVmParameters params = new ImportVmParameters(getEntity(),
                parent.getStorageDomainId(),
                destStorageDomainId,
                parent.getDataCenterId(destStorageDomainId),
                clusterId);
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

        return doAction(ActionType.ImportVm, params, action);
    }

    private void validateClusterMappings(Action action) {
        if (action.getRegistrationConfiguration().isSetClusterMappings()
                && action.getRegistrationConfiguration().getClusterMappings().isSetRegistrationClusterMappings()) {
            action.getRegistrationConfiguration()
                    .getClusterMappings()
                    .getRegistrationClusterMappings()
                    .forEach(this::validateClusterMapping);
        }
    }

    private void validateClusterMapping(RegistrationClusterMapping mapping) {
        if (!mapping.isSetFrom()) {
            badRequest("Cluster name is missing from source.");
        }
        if (!mapping.isSetTo()) {
            badRequest("Cluster name is missing from destination.");
        }
    }

    private void validateRoleMappings(Action action) {
        if (action.getRegistrationConfiguration().isSetRoleMappings()
                && action.getRegistrationConfiguration().getRoleMappings().isSetRegistrationRoleMappings()) {
            action.getRegistrationConfiguration()
                    .getRoleMappings()
                    .getRegistrationRoleMappings()
                    .forEach(this::validateRoleMapping);
        }
    }

    private void validateRoleMapping(RegistrationRoleMapping mapping) {
        if (!mapping.isSetFrom()) {
            badRequest("Role name is missing from source.");
        }
        if (!mapping.isSetTo()) {
            badRequest("Role is missing from destination.");
        }
    }

    private void validateAffinityGroupMappings(Action action) {
        if (action.getRegistrationConfiguration().isSetAffinityGroupMappings()
                && action.getRegistrationConfiguration()
                        .getAffinityGroupMappings()
                        .isSetRegistrationAffinityGroupMappings()) {
            action.getRegistrationConfiguration()
                    .getAffinityGroupMappings()
                    .getRegistrationAffinityGroupMappings()
                    .forEach(this::validateAffinityGroupMapping);
        }
    }

    private void validateAffinityGroupMapping(RegistrationAffinityGroupMapping mapping) {
        if (!mapping.isSetFrom()) {
            badRequest("AffinityGroup name is missing from source.");
        }
        if (!mapping.isSetTo()) {
            badRequest("AffinityGroup name is missing from destination.");
        }
    }

    private void validateAffinityLabelMappings(Action action) {
        if (action.getRegistrationConfiguration().isSetAffinityLabelMappings()
                && action.getRegistrationConfiguration()
                        .getAffinityLabelMappings()
                        .isSetRegistrationAffinityLabelMappings()) {
            action.getRegistrationConfiguration()
                    .getAffinityLabelMappings()
                    .getRegistrationAffinityLabelMappings()
                    .forEach(this::validateAffinityLabelMapping);
        }
    }

    private void validateAffinityLabelMapping(RegistrationAffinityLabelMapping mapping) {
        if (!mapping.isSetFrom()) {
            badRequest("AffinityLabel name is missing from source.");
        }
        if (!mapping.isSetTo()) {
            badRequest("AffinityLabel name is missing from destination.");
        }
    }

    private void validateDomainMappings(Action action) {
        if (action.getRegistrationConfiguration().isSetDomainMappings()
                && action.getRegistrationConfiguration().getDomainMappings().isSetRegistrationDomainMappings()) {
            action.getRegistrationConfiguration()
                    .getDomainMappings()
                    .getRegistrationDomainMappings()
                    .forEach(this::validateDomainMapping);
        }
    }

    private void validateDomainMapping(RegistrationDomainMapping mapping) {
        if (!mapping.isSetFrom()) {
            badRequest("Domain name is missing from source.");
        }
        if (!mapping.isSetTo()) {
            badRequest("Domain name is missing from destination.");
        }
    }

    private void validateLunMappings(Action action) {
        if (action.getRegistrationConfiguration().isSetLunMappings()
                && action.getRegistrationConfiguration().getLunMappings().isSetRegistrationLunMappings()) {
            action.getRegistrationConfiguration()
                    .getLunMappings()
                    .getRegistrationLunMappings()
                    .forEach(this::validateLunMapping);
        }
    }

    private void validateLunMapping(RegistrationLunMapping mapping) {
        if (!mapping.isSetFrom()) {
            badRequest("Lun id is missing from source.");
        }
        if (!mapping.isSetTo()) {
            badRequest("Lun is missing from destination.");
        }
    }

    private boolean getReassignBadMacs(Action action) {
        return action.isSetReassignBadMacs() && action.isReassignBadMacs();
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
    public StorageDomainVmDiskAttachmentsResource getDiskAttachmentsResource() {
        return inject(new BackendExportDomainDiskAttachmentsResource(this));
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
        if (isUnregisteredVm()) {
            return performAction(ActionType.RemoveUnregisteredVm,
                    new RemoveUnregisteredEntityParameters(guid,
                            parent.storageDomainId,
                            getDataCenterId(parent.storageDomainId)));
        }

        get();
        RemoveVmFromImportExportParameters params = new RemoveVmFromImportExportParameters(
                guid,
                parent.storageDomainId,
                getDataCenterId(parent.storageDomainId));
        return performAction(ActionType.RemoveVmFromImportExport, params);
    }

    private boolean isUnregisteredVm() {
        Vm unregisteredVm;
        try {
            unregisteredVm = performGet(QueryType.GetUnregisteredVm,
                    new GetUnregisteredEntityQueryParameters(parent.storageDomainId, guid));
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                return false;
            }
            throw e;
        }

        return unregisteredVm != null;
    }

}
