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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.model.Configuration;
import org.ovirt.engine.api.model.ConfigurationType;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.DiskAttachments;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Initialization;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.Payload;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.Snapshots;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.model.Tags;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.VmPlacementPolicy;
import org.ovirt.engine.api.model.Vms;
import org.ovirt.engine.api.resource.VmResource;
import org.ovirt.engine.api.resource.VmsResource;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.api.restapi.types.RngDeviceMapper;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.api.restapi.util.DisplayHelper;
import org.ovirt.engine.api.restapi.util.IconHelper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.api.restapi.util.VmHelper;
import org.ovirt.engine.core.common.action.AddVmFromSnapshotParameters;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.GetVmFromConfigurationQueryParameters;
import org.ovirt.engine.core.common.queries.GetVmOvfByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.IdsQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmsResource extends
        AbstractBackendCollectionResource<Vm, org.ovirt.engine.core.common.businessentities.VM>
        implements VmsResource {

    static final String[] SUB_COLLECTIONS = {
        "affinitylabels",
        "applications",
        "cdroms",
        "diskattachments",
        "graphicsconsoles",
        "hostdevices",
        "katelloerrata",
        "nics",
        "numanodes",
        "permissions",
        "reporteddevices",
        "sessions",
        "snapshots",
        "statistics",
        "tags",
        "watchdogs",
    };

    public static final String CLONE = "clone";
    public static final String CLONE_PERMISSIONS = "clone_permissions";

    public BackendVmsResource() {
        super(Vm.class, org.ovirt.engine.core.common.businessentities.VM.class, SUB_COLLECTIONS);
    }

    @Override
    public Vms list() {
        if (isFiltered()) {
            return mapCollection(getBackendCollection(VdcQueryType.GetAllVms, new VdcQueryParametersBase(), SearchType.VM), true);
        } else {
            return mapCollection(getBackendCollection(SearchType.VM), false);
        }
    }

    @Override
    public VmResource getVmResource(String id) {
        return inject(new BackendVmResource(id, this));
    }

    @Override
    public Response add(Vm vm) {
        validateParameters(vm, "cluster.id|name");
        validateIconParameters(vm);
        Response response = null;
        if (vm.isSetInitialization() && vm.getInitialization().isSetConfiguration()) {
            validateParameters(vm, "initialization.configuration.type", "initialization.configuration.data");
            response = importVmFromConfiguration(vm);
        } else {
            validateParameters(vm, "name");
            if (isCreateFromSnapshot(vm)) {
                validateSnapshotExistence(vm);
                response = createVmFromSnapshot(vm);
            } else {
                validateParameters(vm, "template.id|name");
                Cluster cluster = getCluster(vm);
                VmTemplate template = lookupTemplate(vm.getTemplate(), cluster.getStoragePoolId());

                VmStatic builtFromTemplate = VmMapper.map(template, null, cluster.getCompatibilityVersion());
                // if VM is based on a template, and going to be on another cluster then template, clear the cpu_profile
                // since the template cpu_profile doesn't match cluster.
                if (!vm.isSetCpuProfile() && vm.isSetCluster()
                        && !Objects.equals(Objects.toString(template.getClusterId(), null), vm.getCluster().getId())) {
                    builtFromTemplate.setCpuProfileId(null);
                }

                VmStatic builtFromInstanceType = null;
                InstanceType instanceTypeEntity = null;
                if (vm.isSetInstanceType() && (vm.getInstanceType().isSetId() || vm.getInstanceType().isSetName())) {
                    instanceTypeEntity = lookupInstance(vm.getInstanceType());
                    builtFromInstanceType = VmMapper.map(instanceTypeEntity, builtFromTemplate, cluster.getCompatibilityVersion());
                    builtFromInstanceType.setInstanceTypeId(instanceTypeEntity.getId());
                }

                VmStatic staticVm = getMapper(Vm.class, VmStatic.class).map(vm, builtFromInstanceType != null ? builtFromInstanceType : builtFromTemplate);
                if (namedCluster(vm)) {
                    staticVm.setClusterId(cluster.getId());
                }

                if (Guid.Empty.equals(template.getId()) && !vm.isSetOs()) {
                    staticVm.setOsId(OsRepository.AUTO_SELECT_OS);
                }

                staticVm.setUsbPolicy(VmMapper.getUsbPolicyOnCreate(vm.getUsb()));

                if (!isFiltered() && vm.isSetPlacementPolicy()) {
                    Set<Guid> hostGuidsSet = validateAndUpdateHostsInPlacementPolicy(vm.getPlacementPolicy());
                    staticVm.setDedicatedVmForVdsList(new LinkedList<>(hostGuidsSet));
                } else {
                    vm.setPlacementPolicy(null);
                }

                // If the user omits the placement policy in the incoming XML and the selected template
                // is the blank one, the AddVmCommand must auto-select a proper default value for the
                // migration support (disabling it in architectures that do not support this feature)
                if (!vm.isSetPlacementPolicy() && template.getId().equals(Guid.Empty)) {
                    staticVm.setMigrationSupport(null);
                }

                Guid storageDomainId =
                        (vm.isSetStorageDomain() && vm.getStorageDomain().isSetId()) ? asGuid(vm.getStorageDomain()
                                .getId())
                                : Guid.Empty;

                boolean clone = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, CLONE, true, false);
                if (clone) {
                    response = cloneVmFromTemplate(staticVm, vm, template, instanceTypeEntity, cluster);
                } else if (Guid.Empty.equals(template.getId())) {
                    response = addVmFromScratch(staticVm, vm, instanceTypeEntity, cluster);
                } else {
                    response = addVm(staticVm, vm, storageDomainId, template, instanceTypeEntity, cluster);
                }
            }
        }

        Vm result = (Vm) response.getEntity();
        if (result != null) {
            DisplayHelper.adjustDisplayData(this, result, false);
            removeRestrictedInfo(result);
        }

        return response;
    }

    private void validateIconParameters(Vm vm) {
        if (!IconHelper.validateIconParameters(vm)) {
            throw new BaseBackendResource.WebFaultException(null,
                    localize(Messages.INVALID_ICON_PARAMETERS),
                    Response.Status.BAD_REQUEST);
        }
    }

    private boolean shouldMakeCreatorExplicitOwner() {
        // In the user level API we should make the creator the owner of the new created machine
        return isFiltered();
    }

    private boolean isCreateFromSnapshot(Vm vm) {
        return vm.isSetSnapshots() && vm.getSnapshots().getSnapshots() != null
                && !vm.getSnapshots().getSnapshots().isEmpty();
    }

    private void validateSnapshotExistence(Vm vm) {
        // null and emptiness were previously tested
        Snapshot snapshot = vm.getSnapshots().getSnapshots().get(0);
        validateParameters(snapshot, "id");
    }

    private Response createVmFromSnapshot(Vm vm) {
        // If Vm has snapshots collection - this is a clone vm from snapshot operation
        String snapshotId = getSnapshotId(vm.getSnapshots());
        org.ovirt.engine.core.common.businessentities.VM vmConfiguration = getVmConfiguration(snapshotId);
        getMapper(Vm.class, VmStatic.class).map(vm, vmConfiguration.getStaticData());
        // If vm passed in the call has disks attached on them,
        // merge their data with the data of the disks on the configuration
        // The parameters to AddVmFromSnapshot hold an array list of Disks
        // and not List of Disks, as this is a GWT serialization limitation,
        // and this parameter class serves GWT clients as well.
        HashMap<Guid, DiskImage> diskImagesByImageId = getDiskImagesByIdMap(vmConfiguration.getDiskMap().values());
        if (vm.isSetDiskAttachments()) {
            prepareImagesForCloneFromSnapshotParams(vm.getDiskAttachments(), diskImagesByImageId);
        }
        return cloneVmFromSnapshot(vmConfiguration,
                vm,
                snapshotId,
                diskImagesByImageId);
    }

    private Vm removeRestrictedInfo(Vm vm) {
        if (isFiltered()) {
            vm.setHost(null);
            vm.setPlacementPolicy(null);
        }
        return vm;
    }

    protected VmPayload getPayload(Vm vm) {
        VmPayload payload = null;
        if (vm.isSetPayloads() && vm.getPayloads().isSetPayloads()) {
            payload = getMapper(Payload.class, VmPayload.class).map(vm.getPayloads().getPayloads().get(0), new VmPayload());
        }
        return payload;
    }

    public Response importVmFromConfiguration(Vm vm) {
        Initialization initialization = vm.getInitialization();
        Configuration config = initialization.getConfiguration();
        org.ovirt.engine.core.common.businessentities.VM vmConfiguration =
                getEntity(org.ovirt.engine.core.common.businessentities.VM.class,
                        VdcQueryType.GetVmFromConfiguration,
                        new GetVmFromConfigurationQueryParameters(VmMapper.map(config.getType(), null), config.getData().trim()),
                        "");

        VmMapper.map(vm, vmConfiguration.getStaticData());

        Guid clusterId = namedCluster(vm) ? getCluster(vm).getId() : asGuid(vm.getCluster().getId());
        ImportVmParameters parameters = new ImportVmParameters();
        parameters.setVm(vmConfiguration);
        parameters.setClusterId(clusterId);
        if (initialization.isSetRegenerateIds()) {
            parameters.setImportAsNewEntity(initialization.isRegenerateIds());
        }
        return performCreate(VdcActionType.ImportVmFromConfiguration,
                parameters,
                new QueryIdResolver<Guid>(VdcQueryType.GetVmByVmId, IdQueryParameters.class));
    }

    protected org.ovirt.engine.core.common.businessentities.VM getVmConfiguration(String snapshotId) {
        org.ovirt.engine.core.common.businessentities.VM vmConfiguration =
                getEntity(org.ovirt.engine.core.common.businessentities.VM.class,
                        VdcQueryType.GetVmConfigurationBySnapshot,
                        new IdQueryParameters(asGuid(snapshotId)),
                        "");
        return vmConfiguration;
    }

    private void prepareImagesForCloneFromSnapshotParams(DiskAttachments disksAttachments,
            Map<Guid, DiskImage> imagesFromConfiguration) {
        if (disksAttachments.getDiskAttachments() != null) {
            for (DiskAttachment diskAttachment : disksAttachments.getDiskAttachments()) {
                Disk disk = diskAttachment.getDisk();
                if (disk != null && disk.isSetImageId()) {
                    DiskImage diskImageFromConfig = imagesFromConfiguration.get(asGuid(disk.getImageId()));
                    DiskImage diskImage = (DiskImage) getMapper(Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class).map(disk, diskImageFromConfig);
                    imagesFromConfiguration.put(diskImage.getId(), diskImage);
                }
            }
        }
    }

    private HashMap<Guid, DiskImage> getDiskImagesByIdMap(Collection<org.ovirt.engine.core.common.businessentities.storage.Disk> values) {
        HashMap<Guid, DiskImage> result = new HashMap<>();
        for (org.ovirt.engine.core.common.businessentities.storage.Disk diskImage : values) {
            result.put(diskImage.getId(), (DiskImage) diskImage);
        }
        return result;
    }

    private String getSnapshotId(Snapshots snapshots) {
        return (snapshots.getSnapshots() != null && !snapshots.getSnapshots().isEmpty()) ? snapshots.getSnapshots()
                .get(0)
                .getId() : Guid.Empty.toString();
    }

    private Response cloneVmFromSnapshot(org.ovirt.engine.core.common.businessentities.VM configVm,
            Vm vm,
            String snapshotId,
            HashMap<Guid, DiskImage> images) {
        VmStatic staticVm = configVm.getStaticData();
        Guid sourceSnapshotId = asGuid(snapshotId);
        AddVmFromSnapshotParameters params =
                new AddVmFromSnapshotParameters(staticVm, sourceSnapshotId);
        params.setDiskInfoDestinationMap(images);
        params.setMakeCreatorExplicitOwner(shouldMakeCreatorExplicitOwner());
        params.setVirtioScsiEnabled(vm.isSetVirtioScsi() && vm.getVirtioScsi().isSetEnabled() ?
                vm.getVirtioScsi().isEnabled() : null);
        if(vm.isSetSoundcardEnabled()) {
            params.setSoundDeviceEnabled(vm.isSoundcardEnabled());
        } else {
            params.setSoundDeviceEnabled(isVMDeviceTypeExist(configVm.getManagedVmDeviceMap(), VmDeviceGeneralType.SOUND));
        }

        params.setConsoleEnabled(vm.isSetConsole() && vm.getConsole().isSetEnabled()
                ? vm.getConsole().isEnabled()
                : !getConsoleDevicesForEntity(staticVm.getId()).isEmpty());

        if (vm.isSetRngDevice()) {
            params.setUpdateRngDevice(true);
            params.setRngDevice(RngDeviceMapper.map(vm.getRngDevice(), null));
        }

        DisplayHelper.setGraphicsToParams(vm.getDisplay(), params);

        return performCreate(VdcActionType.AddVmFromSnapshot,
                                params,
                                new QueryIdResolver<Guid>(VdcQueryType.GetVmByVmId, IdQueryParameters.class));
    }

    private Response cloneVmFromTemplate(VmStatic staticVm, Vm vm, VmTemplate template, InstanceType instanceType, Cluster cluster) {
        AddVmParameters params = new AddVmParameters(staticVm);
        params.setDiskInfoDestinationMap(getDisksToClone(vm.getDiskAttachments(), template.getId()));
        params.setVmPayload(getPayload(vm));

        addDevicesToParams(params, vm, template, instanceType, staticVm.getOsId(), cluster);
        IconHelper.setIconToParams(vm, params);

        params.setMakeCreatorExplicitOwner(shouldMakeCreatorExplicitOwner());
        setupCloneTemplatePermissions(params);
        DisplayHelper.setGraphicsToParams(vm.getDisplay(), params);

        return performCreate(VdcActionType.AddVmFromTemplate,
                params,
                new QueryIdResolver<Guid>(VdcQueryType.GetVmByVmId, IdQueryParameters.class));
    }

    private void addDevicesToParams(AddVmParameters params, Vm vm, VmTemplate template, InstanceType instanceType, int osId, Cluster cluster) {
        Guid templateId = template != null ? template.getId() : null;
        Guid instanceTypeId = instanceType != null ? instanceType.getId() : null;

        if (vm.isSetVirtioScsi()) {
            params.setVirtioScsiEnabled(vm.getVirtioScsi().isEnabled());
        } else {
            // it is not defined on the template
            params.setVirtioScsiEnabled(instanceTypeId != null ? !VmHelper.getVirtioScsiControllersForEntity(this, instanceTypeId).isEmpty() : null);
        }

        if(vm.isSetSoundcardEnabled()) {
            params.setSoundDeviceEnabled(vm.isSoundcardEnabled());
        } else if (instanceTypeId != null || templateId != null) {
            params.setSoundDeviceEnabled(!VmHelper.getSoundDevicesForEntity(this, instanceTypeId != null ? instanceTypeId : templateId).isEmpty());
        }

        if (vm.isSetMemoryPolicy()) {
            params.setBalloonEnabled(vm.getMemoryPolicy().isBallooning());
        } else if (shouldCopyDevice(SimpleDependencyInjector.getInstance().get(OsRepository.class).isBalloonEnabled(osId, cluster.getCompatibilityVersion()), templateId, instanceTypeId)) {
            // it is not defined on the template
            params.setBalloonEnabled(instanceTypeId != null ? !VmHelper.isMemoryBalloonEnabledForEntity(this, instanceTypeId) : null);
        }

        if (vm.isSetConsole()) {
            params.setConsoleEnabled(vm.getConsole().isEnabled());
        } else if (instanceTypeId != null || templateId != null) {
            params.setConsoleEnabled(instanceTypeId != null ? !getConsoleDevicesForEntity(instanceTypeId).isEmpty() : null);
        }

        if (vm.isSetRngDevice()) {
            params.setUpdateRngDevice(true);
            params.setRngDevice(RngDeviceMapper.map(vm.getRngDevice(), null));
        } else if (instanceTypeId != null || templateId != null) {
            List<VmRngDevice> devices = VmHelper.getRngDevicesForEntity(this, instanceTypeId != null ? instanceTypeId : templateId);
            if (devices != null && !devices.isEmpty()) {
                boolean supported = cluster.getRequiredRngSources().contains(devices.get(0).getSource());
                if (shouldCopyDevice(supported, templateId, instanceTypeId)) {
                    params.setUpdateRngDevice(true);
                    params.setRngDevice(!devices.isEmpty() ? devices.iterator().next() : null);
                }
            }
        }
    }

    /**
     * Returns true if the device should be copied from the template or instance type
     * If the instance type is selected, than the device will be copied from the instance type only if the device is compatible with the cluster and os
     * If the instance type is not set and the template is set the
     * compatibility has to be checked as well because the blank template can be set which does not live on a cluster
     */
    private boolean shouldCopyDevice(boolean isCompatibleWithCluster, Guid templateId, Guid instanceTypeId) {
        if (instanceTypeId == null && templateId == null) {
            // nothing to copy from
            return false;
        }

        // copy only if compatible with cluster (instance type or blank template can contain unsupported devices)
        return isCompatibleWithCluster;
    }

    private HashMap<Guid, DiskImage> getDisksToClone(DiskAttachments diskAttachments, Guid templateId) {
        HashMap<Guid, DiskImage> disksMap = new HashMap<>();

        if (diskAttachments != null && diskAttachments.isSetDiskAttachments() && diskAttachments.getDiskAttachments().size() > 0){
            HashMap<Guid, DiskImage> templatesDisksMap = getTemplateDisks(templateId);
            for (DiskAttachment diskAttachment : diskAttachments.getDiskAttachments()) {
                Disk disk = diskAttachment.getDisk();
                if (disk != null && disk.isSetId()) {
                    DiskImage templateDisk = templatesDisksMap.get(asGuid(disk.getId()));
                    if (templateDisk != null) {
                        // when disk profile isn't specified, and disks are cloned to another storage
                        // domain then the original disk, disk profile is cleared since template disk
                        // disk profile isn't matching destination storage domain.
                        if (!disk.isSetDiskProfile()
                            && disk.isSetStorageDomains()
                            && disk.getStorageDomains().isSetStorageDomains()
                            && disk.getStorageDomains().getStorageDomains().get(0).isSetId()
                            && !Objects.equals(disk.getStorageDomains().getStorageDomains().get(0).getId(),
                            Objects.toString(templateDisk.getStorageIds().get(0), null))) {
                            templateDisk.setDiskProfileId(null);
                        }
                        disksMap.put(templateDisk.getId(), map(disk, templateDisk));
                    } else {
                        throw new WebApplicationException(Response.Status.NOT_FOUND);
                    }
                }
            }
        }
        return disksMap;
    }

    @SuppressWarnings("unchecked")
    private HashMap<Guid, DiskImage> getTemplateDisks(Guid templateId) {
        HashMap<Guid, DiskImage> templatesDisksMap = new HashMap<>();
        for (DiskImage di : (List<DiskImage>) getEntity(List.class,
                                                      VdcQueryType.GetVmTemplatesDisks,
                                                      new IdQueryParameters(templateId),
                                                      "Disks")) {
            templatesDisksMap.put(di.getId(), di);
        }
        return templatesDisksMap;
    }

    private DiskImage map(Disk entity, DiskImage template) {
        return (DiskImage)getMapper(Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class).map(entity, template);
    }

    protected Response addVm(VmStatic staticVm, Vm vm, Guid storageDomainId, VmTemplate template, InstanceType instanceType, Cluster cluster) {
        AddVmParameters params = new AddVmParameters(staticVm);
        params.setVmPayload(getPayload(vm));
        params.setStorageDomainId(storageDomainId);
        params.setDiskInfoDestinationMap(getDisksToClone(vm.getDiskAttachments(), template.getId()));
        params.setMakeCreatorExplicitOwner(shouldMakeCreatorExplicitOwner());
        setupCloneTemplatePermissions(params);
        addDevicesToParams(params, vm, template, instanceType, staticVm.getOsId(), cluster);
        IconHelper.setIconToParams(vm, params);
        DisplayHelper.setGraphicsToParams(vm.getDisplay(), params);

        return performCreate(VdcActionType.AddVm,
                               params,
                               new QueryIdResolver<Guid>(VdcQueryType.GetVmByVmId, IdQueryParameters.class));
    }

    void setupCloneTemplatePermissions(VmManagementParametersBase params) {
        boolean clonePermissions = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, CLONE_PERMISSIONS, true, false);
        if (clonePermissions) {
            params.setCopyTemplatePermissions(clonePermissions);
        }
    }

    protected Response addVmFromScratch(VmStatic staticVm, Vm vm, InstanceType instanceType, Cluster cluster) {
        AddVmParameters params = new AddVmParameters(staticVm);
        params.setVmPayload(getPayload(vm));
        params.setMakeCreatorExplicitOwner(shouldMakeCreatorExplicitOwner());
        addDevicesToParams(params, vm, null, instanceType, staticVm.getOsId(), cluster);
        IconHelper.setIconToParams(vm, params);
        DisplayHelper.setGraphicsToParams(vm.getDisplay(), params);

        return performCreate(VdcActionType.AddVmFromScratch,
                               params,
                               new QueryIdResolver<Guid>(VdcQueryType.GetVmByVmId, IdQueryParameters.class));
    }

    public ArrayList<DiskImage> mapDisks(Disks disks) {
        ArrayList<DiskImage> diskImages = null;
        if (disks!=null && disks.isSetDisks()) {
            diskImages = new ArrayList<>();
            for (Disk disk : disks.getDisks()) {
                DiskImage diskImage = (DiskImage)DiskMapper.map(disk, null);
                diskImages.add(diskImage);
            }
        }
        return diskImages;
    }

    private void addInlineStatistics(Vm vm) {
        EntityIdResolver<Guid> resolver = new QueryIdResolver<>(VdcQueryType.GetVmByVmId, IdQueryParameters.class);
        VmStatisticalQuery query = new VmStatisticalQuery(resolver, newModel(vm.getId()));
        BackendStatisticsResource<Vm, org.ovirt.engine.core.common.businessentities.VM> statisticsResource = inject(new BackendStatisticsResource<>(entityType, Guid.createGuidFromStringDefaultEmpty(vm.getId()), query));
        Statistics statistics = statisticsResource.list();
        vm.setStatistics(statistics);
    }

    private void addInlineTags(Vm vm) {
        BackendVmTagsResource tagsResource = inject(new BackendVmTagsResource(vm.getId()));
        Tags tags = tagsResource.list();
        vm.setTags(tags);
    }

    private void addInlineNics(Vm vm) {
        Guid vmId = asGuid(vm.getId());
        BackendVmNicsResource nicsResource = inject(new BackendVmNicsResource(vmId));
        Nics nics = nicsResource.list();
        vm.setNics(nics);
    }

    private void addInlineDisks(Vm vm) {
        Guid vmId = asGuid(vm.getId());
        BackendDiskAttachmentsResource disksAttachmentsResource = inject(new BackendDiskAttachmentsResource(vmId));
        DiskAttachments diskAttachments = disksAttachmentsResource.list();
        vm.setDiskAttachments(diskAttachments);
    }

    protected Vms mapCollection(List<org.ovirt.engine.core.common.businessentities.VM> entities, boolean isFiltered) {
        Set<String> details = DetailHelper.getDetails(httpHeaders, uriInfo);
        boolean includeData = details.contains(DetailHelper.MAIN);
        boolean includeSize = details.contains("size");

        List<Guid> vmIds = entities.stream().map(VM::getId).collect(Collectors.toList());
        if (includeData) {
            // Fill VmInit for entities - the search query no join the VmInit to Vm
            IdsQueryParameters params = new IdsQueryParameters();
            params.setId(vmIds);
            VdcQueryReturnValue queryReturnValue = runQuery(VdcQueryType.GetVmsInit, params);
            if (queryReturnValue.getSucceeded() && queryReturnValue.getReturnValue() != null) {
                List<VmInit> vmInits = queryReturnValue.getReturnValue();
                Map<Guid, VmInit> initMap = Entities.businessEntitiesById(vmInits);
                for (org.ovirt.engine.core.common.businessentities.VM vm : entities) {
                    vm.setVmInit(initMap.get(vm.getId()));
                }
            }
        }

        Vms collection = new Vms();
        if (includeData) {
            // optimization of DB access: retrieve GraphicsDevices for all VMs at once
            Map<Guid, List<GraphicsDevice>> vmsGraphicsDevices =
                    DisplayHelper.getGraphicsDevicesForMultipleEntities(this, vmIds);

            for (org.ovirt.engine.core.common.businessentities.VM entity : entities) {
                Vm vm = map(entity);
                DisplayHelper.adjustDisplayData(this, vm, vmsGraphicsDevices, false);
                removeRestrictedInfo(vm);
                collection.getVms().add(addLinks(populate(vm, entity)));
            }
        }
        if (includeSize) {
            collection.setSize((long) entities.size());
        }
        return collection;
    }

    protected boolean templated(Vm vm) {
        return vm.isSetTemplate() && (vm.getTemplate().isSetId() || vm.getTemplate().isSetName());
    }

    protected InstanceType lookupInstance(Template template) {
        return getEntity(InstanceType.class,
                VdcQueryType.GetInstanceType,
                new GetVmTemplateParameters(asGuid(template.getId())),
                "GetInstanceType");
    }

    protected VmTemplate lookupTemplate(Template template, Guid datacenterId) {
        if (template.isSetId()) {
            return getEntity(VmTemplate.class,
                    VdcQueryType.GetVmTemplate,
                    new GetVmTemplateParameters(asGuid(template.getId())),
                    "GetVmTemplate");
        } else if (template.isSetName()) {
            GetVmTemplateParameters params = new GetVmTemplateParameters(template.getName());
            params.setDataCenterId(datacenterId);
            return getEntity(VmTemplate.class, VdcQueryType.GetVmTemplate, params, "GetVmTemplate");
        }
        return null; // should never happen.
    }

    public VmTemplate lookupTemplate(Guid id) {
        return getEntity(VmTemplate.class, VdcQueryType.GetVmTemplate, new GetVmTemplateParameters(id), "GetVmTemplate");
    }

    private Cluster lookupCluster(Guid id) {
        return getEntity(Cluster.class, VdcQueryType.GetClusterByClusterId, new IdQueryParameters(id), "GetClusterByClusterId");
    }

    protected boolean namedCluster(Vm vm) {
        return vm.isSetCluster() && vm.getCluster().isSetName() && !vm.getCluster().isSetId();
    }

    protected Cluster getCluster(Vm vm) {
        if (namedCluster(vm)) {
            return isFiltered() ? lookupClusterByName(vm.getCluster().getName()) : getEntity(Cluster.class,
                    VdcQueryType.GetClusterByName,
                    new NameQueryParameters(vm.getCluster().getName()),
                    "Cluster: name=" + vm.getCluster().getName());
        }

        return lookupCluster(asGuid(vm.getCluster().getId()));
    }

    public Cluster lookupClusterByName(String name) {
        return getEntity(Cluster.class, VdcQueryType.GetClusterByName, new NameQueryParameters(name), "GetClusterByName");
    }

    protected Vm setVmOvfConfiguration (Vm model, org.ovirt.engine.core.common.businessentities.VM entity) {
        VdcQueryReturnValue queryReturnValue =
                runQuery(VdcQueryType.GetVmOvfByVmId,
                        new GetVmOvfByVmIdParameters(entity.getId(), entity.getDbGeneration()));

        if (queryReturnValue.getSucceeded() && queryReturnValue.getReturnValue() != null) {
            String configuration = queryReturnValue.getReturnValue();
            return VmMapper.map(configuration,
                    ConfigurationType.OVF,
                    model);
        }

        return model;
    }

    @Override
    protected Vm deprecatedPopulate(Vm model, org.ovirt.engine.core.common.businessentities.VM entity) {
        Set<String> details = DetailHelper.getDetails(httpHeaders, uriInfo);
        if (details.contains("statistics")) {
            addInlineStatistics(model);
        }
        return model;
    }

    @Override
    protected Vm doPopulate(Vm model, org.ovirt.engine.core.common.businessentities.VM entity) {
        BackendVmDeviceHelper.setPayload(this, model);
        MemoryPolicyHelper.setupMemoryBalloon(model, this);
        BackendVmDeviceHelper.setConsoleDevice(this, model);
        BackendVmDeviceHelper.setVirtioScsiController(this, model);
        BackendVmDeviceHelper.setSoundcard(this, model);
        BackendVmDeviceHelper.setCertificateInfo(this, model);
        BackendVmDeviceHelper.setRngDevice(this, model);
        setVmOvfConfiguration(model, entity);
        return model;
    }

    private List<String> getConsoleDevicesForEntity(Guid id) {
        return getEntity(List.class,
                VdcQueryType.GetConsoleDevices,
                new IdQueryParameters(id),
                "GetConsoleDevices", true);
    }

    private boolean isVMDeviceTypeExist(Map<Guid, VmDevice> deviceMap, VmDeviceGeneralType deviceType) {
        if(deviceMap != null) {
            for (Map.Entry<Guid, VmDevice> device : deviceMap.entrySet()) {
                if (device.getValue().getType().equals(deviceType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Update and validate PlacementPolicy object
     * Fill hostId for host elements specified by name
     * Returns Set of dedicated hosts' Guids found by name or id in PlacementPolicy
     */
    protected Set<Guid> validateAndUpdateHostsInPlacementPolicy(VmPlacementPolicy placementPolicy) {
        Set<Guid> hostsGuidsSet = new HashSet<>();
        if (placementPolicy.isSetHosts()
                && placementPolicy.getHosts().getHosts().size() > 0) {
            for (Host host : placementPolicy.getHosts().getHosts()) {
                validateParameters(host, "id|name");
                // for each host that is specified by name or id
                updateIdForSingleHost(host, hostsGuidsSet);
            }
        }
        return hostsGuidsSet;
    }

    private void updateIdForSingleHost(Host host, Set<Guid> guidsSet) {
        if (host.isSetName() && !host.isSetId()){
            // find the corresponding host id
            Guid hostGuid = getHostId(host);
            if (hostGuid != null) {
                guidsSet.add(hostGuid);
                // add hostId element to host
                host.setId(hostGuid.toString());
            }
        } else if (host.isSetId()){
            guidsSet.add(Guid.createGuidFromString(host.getId()));
        }
    }
}
