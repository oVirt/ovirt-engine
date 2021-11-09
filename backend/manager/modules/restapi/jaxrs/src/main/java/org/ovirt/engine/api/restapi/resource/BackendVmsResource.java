/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.model.ActionableResource;
import org.ovirt.engine.api.model.AutoPinningPolicy;
import org.ovirt.engine.api.model.Configuration;
import org.ovirt.engine.api.model.ConfigurationType;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.DiskAttachments;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.GraphicsConsoles;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostDevices;
import org.ovirt.engine.api.model.Initialization;
import org.ovirt.engine.api.model.Payload;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.Snapshots;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.VmPlacementPolicy;
import org.ovirt.engine.api.model.Vms;
import org.ovirt.engine.api.resource.VmResource;
import org.ovirt.engine.api.resource.VmsResource;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.resource.utils.LinksTreeNode;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.api.restapi.types.RngDeviceMapper;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.api.restapi.util.DisplayHelper;
import org.ovirt.engine.api.restapi.util.IconHelper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.api.restapi.util.QueryHelper;
import org.ovirt.engine.api.restapi.util.VmHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmFromSnapshotParameters;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.ImportVmFromConfParameters;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.GetFilteredAndSortedParameters;
import org.ovirt.engine.core.common.queries.GetVmFromConfigurationQueryParameters;
import org.ovirt.engine.core.common.queries.GetVmOvfByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.IdsQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.CompatibilityVersionUtils;
import org.ovirt.engine.core.common.utils.VmCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.RngUtils;

public class BackendVmsResource extends
        AbstractBackendCollectionResource<Vm, org.ovirt.engine.core.common.businessentities.VM>
        implements VmsResource {

    public static final String CLONE = "clone";
    public static final String CLONE_PERMISSIONS = "clone_permissions";
    public static final String OVF_AS_OVA = "ovf_as_ova";
    public static final String AUTO_PINNING_POLICY = "auto_pinning_policy";
    private static final String LEGAL_CLUSTER_COMPATIBILITY_VERSIONS =
            Version.ALL.stream().map(Version::toString).collect(Collectors.joining(", "));
    private static final String CURRENT_GRAPHICS_CONSOLES = "current_graphics_consoles";
    private static final String GRAPHICS_CONSOLES = "graphics_consoles";
    private static final String CD_ROMS = "cdroms";
    private static final String HOST_DEVICES = "host_devices";
    private static final String WATCHDOGS = "watchdogs";
    private static final String SNAPSHOTS = "snapshots";

    private Map<String, VM> vmIdToVm = Collections.emptyMap();

    public BackendVmsResource() {
        super(Vm.class, org.ovirt.engine.core.common.businessentities.VM.class);
    }

    @Override
    public Vms list() {
        if (isFiltered()) {
            if (isSortedAndMaxResults()) { //Specific use-case of ovirt-web-ui
                return getVmsFilteredAndSorted();
            } else {
                return mapCollection(getBackendCollection(QueryType.GetAllVms, new QueryParametersBase(), SearchType.VM));
            }
        } else {
            return mapCollection(getBackendCollection(SearchType.VM));
        }
    }


    /**
     * Check for a combination of sorting ("sortby name asc) and specification of
     * max results ('max' URL parameter). This is a use-case of the User-Portal
     * that requires specific handling.
     */
    private boolean isSortedAndMaxResults() {
        String searchConstraint = QueryHelper.getConstraint(httpHeaders, uriInfo, "", modelType);
        int max = ParametersHelper.getIntegerParameter(httpHeaders, uriInfo, "max", -1, -1);
        return searchConstraint != null
                && !searchConstraint.isEmpty()
                && searchConstraint.toLowerCase().contains("sortby name asc")
                && max != -1;
    }

    /**
     * Specific use-case in the User-Portal - Get vms:
     *
     *   1) filtered by user
     *   2) sorted (ascending order)
     *   3) with max # of results specified.
     *   4) potentially with page number (paging)
     *
     * The engine does not support search + filtering simultaneously.
     * The API supports this using an intersection of two queries, but
     * can not consider max results as well. This is why a designated
     * query is needed.
     *
     * (https://bugzilla.redhat.com/1534607)
     */
    private Vms getVmsFilteredAndSorted() {
        int max = ParametersHelper.getIntegerParameter(httpHeaders, uriInfo, "max", -1, -1);
        String searchConstraint = QueryHelper.getConstraint(httpHeaders, uriInfo, "", modelType);
        Integer pageNum = QueryHelper.parsePageNum(searchConstraint);
        GetFilteredAndSortedParameters params = new GetFilteredAndSortedParameters(max, pageNum == null ? 1 : pageNum);
        return mapCollection(getBackendCollection(QueryType.GetAllVmsFilteredAndSorted, params));
    }

    @Override
    public VmResource getVmResource(String id) {
        return inject(new BackendVmResource(id, this));
    }

    @Override
    public Response add(Vm vm) {
        validateParameters(vm, "cluster.id|name");
        validateIconParameters(vm);
        // validate that the provided cluster-compatibility-version is legal
        validateClusterCompatibilityVersion(vm);
        validateVirtioScsiMultiQueues(vm);
        Response response = null;
        if (vm.isSetInitialization() && vm.getInitialization().isSetConfiguration()) {
            validateParameters(vm, "initialization.configuration.type", "initialization.configuration.data");
            response = importVmFromConfiguration(vm);
        } else {
            validateParameters(vm, "name");
            if (isCreateFromSnapshot(vm)) {
                validateSnapshotExistence(vm);
                Cluster cluster = getCluster(vm);
                response = createVmFromSnapshot(vm, cluster);
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

                updateMaxMemoryIfUnspecified(vm, staticVm);
                updateMinAllocatedMemoryIfUnspecified(vm, staticVm, cluster);

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

                updateCpuPinningFields(staticVm);

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

    void validateClusterCompatibilityVersion(Vm vm) {
        if (vm.isSetCustomCompatibilityVersion()
                && vm.getCustomCompatibilityVersion().isSetMajor()
                && vm.getCustomCompatibilityVersion().isSetMinor()) {
            int major = vm.getCustomCompatibilityVersion().getMajor();
            int minor = vm.getCustomCompatibilityVersion().getMinor();
            if (!isLegalClusterCompatibilityVersion(major, minor)) {
                throw new WebFaultException(null,
                        localize(Messages.INVALID_VERSION_REASON),
                        localize(Messages.INVALID_VERSION_DETAIL, LEGAL_CLUSTER_COMPATIBILITY_VERSIONS),
                        Response.Status.BAD_REQUEST);
            }
        }
    }

    void validateVirtioScsiMultiQueues(Vm vm) {
        if (vm.isSetVirtioScsiMultiQueues() && vm.isSetVirtioScsiMultiQueuesEnabled()
                && !vm.isVirtioScsiMultiQueuesEnabled()) {
            throw new WebFaultException(null,
                    localize(Messages.INVALID_VIRTIO_SCSI_MULTI_QUEUE_REASON),
                    localize(Messages.INVALID_VIRTIO_SCSI_MULTI_QUEUE_DETAIL),
                    Response.Status.BAD_REQUEST);
        }
    }

    private boolean isLegalClusterCompatibilityVersion(int major, int minor) {
        return Version.ALL.contains(new Version(major, minor));
    }

    private void updateMaxMemoryIfUnspecified(Vm vm, VmStatic vmStatic) {
        if (!(vm.isSetMemoryPolicy() && vm.getMemoryPolicy().isSetMax()) && vm.isSetMemory()) {
            vmStatic.setMaxMemorySizeMb(VmCommonUtils.getMaxMemorySizeDefault(vmStatic.getMemSizeMb()));
        }
    }

    private void updateMinAllocatedMemoryIfUnspecified(Vm vm, VmStatic vmStatic, Cluster cluster) {
        if (!(vm.isSetMemoryPolicy() && vm.getMemoryPolicy().isSetGuaranteed()) && vm.isSetMemory()) {
            int minMemory = VmCommonUtils.calcMinMemory(
                    vmStatic.getMemSizeMb(), cluster.getMaxVdsMemoryOverCommit());
            vmStatic.setMinAllocatedMem(minMemory);
        }
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

    private Response createVmFromSnapshot(Vm vm, Cluster cluster) {
        // If Vm has snapshots collection - this is a clone vm from snapshot operation
        String snapshotId = getSnapshotId(vm.getSnapshots());
        org.ovirt.engine.core.common.businessentities.VM vmConfiguration = getVmConfiguration(snapshotId);
        getMapper(Vm.class, VmStatic.class).map(vm, vmConfiguration.getStaticData());
        updateMaxMemoryIfUnspecified(vm, vmConfiguration.getStaticData());
        updateMinAllocatedMemoryIfUnspecified(vm, vmConfiguration.getStaticData(), cluster);
        // If vm passed in the call has disks attached on them,
        // merge their data with the data of the disks on the configuration
        // The parameters to AddVmFromSnapshot hold an array list of Disks
        // and not List of Disks, as this is a GWT serialization limitation,
        // and this parameter class serves GWT clients as well.
        Map<Guid, DiskImage> diskImagesByImageId = getDiskImagesByIdMap(vmConfiguration.getDiskMap().values());
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
                        QueryType.GetVmFromConfiguration,
                        new GetVmFromConfigurationQueryParameters(VmMapper.map(config.getType(), null), config.getData().trim()),
                        "");

        vm.setInitialization(null); // if configuration is provided, the initialization parameters cannot be overridden
        VmMapper.map(vm, vmConfiguration.getStaticData());

        Guid clusterId = namedCluster(vm) ? getCluster(vm).getId() : asGuid(vm.getCluster().getId());
        ImportVmFromConfParameters parameters = new ImportVmFromConfParameters();
        vmConfiguration.setVmtGuid(Guid.Empty);
        parameters.setVm(vmConfiguration);
        parameters.setClusterId(clusterId);
        if (initialization.isSetRegenerateIds()) {
            parameters.setImportAsNewEntity(initialization.isRegenerateIds());
        }
        return performCreate(ActionType.ImportVmFromConfiguration,
                parameters,
                PollingType.JOB,
                new QueryIdResolver<Guid>(QueryType.GetVmByVmId, IdQueryParameters.class));
    }

    protected org.ovirt.engine.core.common.businessentities.VM getVmConfiguration(String snapshotId) {
        org.ovirt.engine.core.common.businessentities.VM vmConfiguration =
                getEntity(org.ovirt.engine.core.common.businessentities.VM.class,
                        QueryType.GetVmConfigurationBySnapshot,
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

    private Map<Guid, DiskImage> getDiskImagesByIdMap(Collection<org.ovirt.engine.core.common.businessentities.storage.Disk> values) {
        return values.stream().collect(Collectors.toMap(BaseDisk::getId, DiskImage.class::cast));
    }

    private String getSnapshotId(Snapshots snapshots) {
        return (snapshots.getSnapshots() != null && !snapshots.getSnapshots().isEmpty()) ? snapshots.getSnapshots()
                .get(0)
                .getId() : Guid.Empty.toString();
    }

    private Response cloneVmFromSnapshot(org.ovirt.engine.core.common.businessentities.VM configVm,
            Vm vm,
            String snapshotId,
            Map<Guid, DiskImage> images) {
        VmStatic staticVm = configVm.getStaticData();
        Guid sourceSnapshotId = asGuid(snapshotId);
        AddVmFromSnapshotParameters params =
                new AddVmFromSnapshotParameters(staticVm, sourceSnapshotId);
        params.setDiskInfoDestinationMap(images);
        params.setMakeCreatorExplicitOwner(shouldMakeCreatorExplicitOwner());
        params.setVirtioScsiEnabled(vm.isSetVirtioScsi() && vm.getVirtioScsi().isSetEnabled() ?
                vm.getVirtioScsi().isEnabled() : null);
        if (vm.isSetSoundcardEnabled()) {
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
        if (vm.isSetTpmEnabled()) {
            params.setTpmEnabled(vm.isTpmEnabled());
        }

        DisplayHelper.setGraphicsToParams(vm.getDisplay(), params);

        return performCreate(ActionType.AddVmFromSnapshot,
                                params,
                                new QueryIdResolver<Guid>(QueryType.GetVmByVmId, IdQueryParameters.class));
    }

    private Response cloneVmFromTemplate(VmStatic staticVm, Vm vm, VmTemplate template, InstanceType instanceType, Cluster cluster) {
        AddVmParameters params = new AddVmParameters(staticVm);
        params.setDiskInfoDestinationMap(getDisksToClone(vm.getDiskAttachments(), template.getId()));
        params.setVmPayload(getPayload(vm));

        addDevicesToParams(params, vm, template, instanceType, staticVm, cluster);
        IconHelper.setIconToParams(vm, params);

        params.setMakeCreatorExplicitOwner(shouldMakeCreatorExplicitOwner());
        addAutoPinningPolicy(vm, params);
        setupCloneTemplatePermissions(params);
        DisplayHelper.setGraphicsToParams(vm.getDisplay(), params);

        return performCreate(ActionType.AddVmFromTemplate,
                params,
                new QueryIdResolver<Guid>(QueryType.GetVmByVmId, IdQueryParameters.class));
    }

    private void addDevicesToParams(
            AddVmParameters params,
            Vm vm, VmTemplate template,
            InstanceType instanceType,
            VmStatic vmStatic,
            Cluster cluster) {
        Guid templateId = template != null ? template.getId() : null;
        Guid instanceTypeId = instanceType != null ? instanceType.getId() : null;

        if (vm.isSetVirtioScsi()) {
            params.setVirtioScsiEnabled(vm.getVirtioScsi().isEnabled());
        } else {
            // it is not defined on the template
            params.setVirtioScsiEnabled(instanceTypeId != null ? !VmHelper.getVirtioScsiControllersForEntity(this, instanceTypeId).isEmpty() : null);
        }

        if (vm.isSetSoundcardEnabled()) {
            params.setSoundDeviceEnabled(vm.isSoundcardEnabled());
        } else if (instanceTypeId != null || templateId != null) {
            params.setSoundDeviceEnabled(!VmHelper.getSoundDevicesForEntity(this, instanceTypeId != null ? instanceTypeId : templateId).isEmpty());
        }

        if (vm.isSetConsole()) {
            params.setConsoleEnabled(vm.getConsole().isEnabled());
        } else if (templateId != null || instanceTypeId != null) {
            params.setConsoleEnabled(!getConsoleDevicesForEntity(instanceTypeId != null ? instanceTypeId : templateId).isEmpty());
        }

        if (vm.isSetRngDevice()) {
            params.setUpdateRngDevice(true);
            params.setRngDevice(RngDeviceMapper.map(vm.getRngDevice(), null));
        } else if (instanceTypeId != null || templateId != null) {
            copyRngDeviceFromTemplateOrInstanceType(params, vmStatic, cluster, templateId, instanceTypeId);
        }

        if (vm.isSetTpmEnabled()) {
            params.setTpmEnabled(vm.isTpmEnabled());
        } else if (instanceTypeId != null || templateId != null) {
            params.setTpmEnabled(!VmHelper.getTpmDevicesForEntity(this, instanceTypeId != null ? instanceTypeId : templateId).isEmpty());
        }
    }

    // TODO: Move user input and template/instance-type merging code to backed
    private void copyRngDeviceFromTemplateOrInstanceType(AddVmParameters params,
            VmStatic vmStatic,
            Cluster cluster,
            Guid templateId,
            Guid instanceTypeId) {
        List<VmRngDevice> devices = VmHelper.getRngDevicesForEntity(
                this, instanceTypeId != null ? instanceTypeId : templateId);
        if (devices != null && !devices.isEmpty()) {
            final VmRngDevice rngDevice = devices.get(0);
            final Version effectiveVersion =
                    CompatibilityVersionUtils.getEffective(vmStatic.getCustomCompatibilityVersion(),
                            cluster.getCompatibilityVersion(),
                            null);
            rngDevice.updateSourceByVersion(effectiveVersion);
            boolean supported = EnumSet.of(
                    RngUtils.RngValidationResult.VALID, RngUtils.RngValidationResult.UNSUPPORTED_URANDOM_OR_RANDOM)
                    .contains(RngUtils.validate(cluster, rngDevice));
            if (shouldCopyDevice(supported, templateId, instanceTypeId)) {
                params.setUpdateRngDevice(true);
                params.setRngDevice(rngDevice);
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

    private Map<Guid, DiskImage> getDisksToClone(DiskAttachments diskAttachments, Guid templateId) {
        Map<Guid, DiskImage> disksMap = new HashMap<>();

        if (diskAttachments != null && diskAttachments.isSetDiskAttachments() && diskAttachments.getDiskAttachments().size() > 0){
            Map<Guid, DiskImage> templatesDisksMap = getTemplateDisks(templateId);
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
    private Map<Guid, DiskImage> getTemplateDisks(Guid templateId) {
        return ((List<DiskImage>) getEntity
                (List.class, QueryType.GetVmTemplatesDisks, new IdQueryParameters(templateId), "Disks"))
                .stream()
                .collect(Collectors.toMap(BaseDisk::getId, Function.identity()));
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
        addAutoPinningPolicy(vm, params);
        setupCloneTemplatePermissions(params);
        addDevicesToParams(params, vm, template, instanceType, staticVm, cluster);
        IconHelper.setIconToParams(vm, params);
        DisplayHelper.setGraphicsToParams(vm.getDisplay(), params);

        return performCreate(ActionType.AddVm,
                               params,
                               new QueryIdResolver<Guid>(QueryType.GetVmByVmId, IdQueryParameters.class));
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
        addAutoPinningPolicy(vm, params);
        addDevicesToParams(params, vm, null, instanceType, staticVm, cluster);
        IconHelper.setIconToParams(vm, params);
        DisplayHelper.setGraphicsToParams(vm.getDisplay(), params);

        return performCreate(ActionType.AddVmFromScratch,
                               params,
                               new QueryIdResolver<Guid>(QueryType.GetVmByVmId, IdQueryParameters.class));
    }

    public List<DiskImage> mapDisks(Disks disks) {
        if (disks!=null && disks.isSetDisks()) {
            return disks.getDisks().stream().map(d -> (DiskImage)DiskMapper.map(d, null)).collect(Collectors.toList());
        }
        return null;
    }

    private void addAutoPinningPolicy(Vm vm, VmManagementParametersBase params) {
        if (vm.getAutoPinningPolicy() != null || vm.getCpuPinningPolicy() != null) {
            return;
        }
        String autoPinningPolicy = ParametersHelper.getParameter(httpHeaders, uriInfo, AUTO_PINNING_POLICY);
        if (autoPinningPolicy != null && !autoPinningPolicy.isEmpty()) {
            if (vm.isSetCpu() && (vm.getCpu().isSetTopology() || vm.getCpu().isSetCpuTune())) {
                throw new WebFaultException(null, localize(Messages.CPU_UPDATE_NOT_PERMITTED), Response.Status.CONFLICT);
            }
            try {
                params.getVm().setCpuPinningPolicy(VmMapper.map(AutoPinningPolicy.fromValue(autoPinningPolicy)));
            } catch (Exception e) {
                throw new WebFaultException(null, localize(Messages.INVALID_ENUM_REASON), Response.Status.BAD_REQUEST);
            }
        }
    }

    private void addInlineStatistics(Vm vm) {
        EntityIdResolver<Guid> resolver = new QueryIdResolver<>(QueryType.GetVmByVmId, IdQueryParameters.class);
        VmStatisticalQuery query = new VmStatisticalQuery(resolver, newModel(vm.getId()));
        BackendStatisticsResource<Vm, org.ovirt.engine.core.common.businessentities.VM> statisticsResource = inject(new BackendStatisticsResource<>(entityType, Guid.createGuidFromStringDefaultEmpty(vm.getId()), query));
        Statistics statistics = statisticsResource.list();
        vm.setStatistics(statistics);
    }

    protected Vms mapCollection(List<org.ovirt.engine.core.common.businessentities.VM> entities) {
        Set<String> details = DetailHelper.getDetails(httpHeaders, uriInfo);
        boolean includeData = details.contains(DetailHelper.MAIN);
        boolean includeSize = details.contains("size");
        boolean includeCurrentGraphicsConsoles = details.contains(CURRENT_GRAPHICS_CONSOLES);

        List<Guid> vmIds = entities.stream().map(VM::getId).collect(Collectors.toList());
        String followValue = ParametersHelper.getParameter(getHttpHeaders(), getUriInfo(), FOLLOW);
        if (StringUtils.isNotEmpty(followValue)) {
            vmIdToVm = entities.stream().collect(Collectors.toMap(vm -> vm.getId().toString(), vm -> vm));
        }

        if (includeData) {
            // Fill VmInit for entities - the search query no join the VmInit to Vm
            IdsQueryParameters params = new IdsQueryParameters();
            params.setId(vmIds);
            QueryReturnValue queryReturnValue = runQuery(QueryType.GetVmsInit, params);
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
                if (includeCurrentGraphicsConsoles) {
                    GraphicsConsoles consoles = new GraphicsConsoles();
                    for (Map.Entry<GraphicsType, GraphicsInfo> entry : entity.getGraphicsInfos().entrySet()) {
                        consoles.getGraphicsConsoles().add(VmMapper.map(entry, null));
                    }
                    vm.setGraphicsConsoles(consoles);
                }
                DisplayHelper.adjustDisplayData(this, vm, vmsGraphicsDevices, false);
                DisplayHelper.addDisplayCertificate(this, vm);
                removeRestrictedInfo(vm);
                collection.getVms().add(addLinks(populate(vm, entity)));
            }
        }
        if (includeSize) {
            collection.setSize((long) entities.size());
        }
        return collection;
    }

    @Override
    public void follow(ActionableResource entity, LinksTreeNode linksTree) {
        super.follow(entity, linksTree);
        if(DetailHelper.getDetails(httpHeaders, uriInfo).contains(CURRENT_GRAPHICS_CONSOLES)) {
            // "?detail=current_graphics_consoles" provides the same output as
            // "?current&follow=graphics_consoles" and similar as "current" flag will
            // overwrite the default output of "?follow=graphics_consoles"
            findGraphicsConsoles(linksTree).ifPresent(node -> node.setFollowed(true));
        }
        findCdroms(linksTree).ifPresent(node -> {
            Vms vms = (Vms) entity;
            vms.getVms().forEach(this::setCdroms);
            node.setFollowed(true);
        });
        findHostDevices(linksTree).ifPresent(node -> {
            Vms vms = (Vms) entity;
            boolean nonePinned = vms.getVms().stream()
                    .map(vm -> vmIdToVm.get(vm.getId()))
                    .map(VM::getDedicatedVmForVdsList)
                    .allMatch(List::isEmpty);
            if (nonePinned) {
                vms.getVms().forEach(this::setEmptyHostDevices);
                node.setFollowed(true);
            }
        });
        findWatchdogs(linksTree).ifPresent(node -> {
            Vms vms = (Vms) entity;
            List<VmWatchdog> watchdogs = getWatchdogs(vms);
            vms.getVms().forEach(vm -> {
                Guid vmId = asGuid(vm.getId());
                setWatchdogs(vm, watchdogs.stream()
                        .filter(w -> vmId.equals(w.getVmId()))
                        .collect(Collectors.toList()));
            });
            node.setFollowed(true);
        });
        findSnapshots(linksTree).ifPresent(node -> {
            Vms vms = (Vms) entity;
            vms.getVms().forEach(this::setSnapshots);
            node.setFollowed(true);
        });
    }

    private List<VmWatchdog> getWatchdogs(Vms vms) {
        List<Guid> vmIds = vms.getVms().stream().map(Vm::getId).map(this::asGuid).collect(Collectors.toList());
        return getEntity(List.class,
                QueryType.GetWatchdogs,
                new IdsQueryParameters(vmIds),
                "GetWatchdogs", true);
    }

    private void setWatchdogs(Vm vm, List<VmWatchdog> watchdogs) {
        vm.setWatchdogs(getBackendVmWatchdogsResource(vm.getId(), watchdogs).list());
    }

    private void setEmptyHostDevices(Vm vm) {
        vm.setHostDevices(new HostDevices());
    }

    private void setCdroms(Vm vm) {
        vm.setCdroms(getBackendVmCdromsResource(vmIdToVm.get(vm.getId())).list());
    }

    private BackendVmCdromsResource getBackendVmCdromsResource(VM vm) {
        return inject(new BackendVmCdromsResource(vm));
    }

    private BackendVmWatchdogsResource getBackendVmWatchdogsResource(String vmId, List<VmWatchdog> watchdogs) {
        return inject(new BackendVmWatchdogsResource(asGuid(vmId), watchdogs));
    }

    private void setSnapshots(Vm vm) {
        vm.setSnapshots(getBackendSnapshotsResource(vm.getId()).list());
    }

    private BackendSnapshotsResource getBackendSnapshotsResource(String vmId) {
        return inject(new BackendSnapshotsResource(asGuid(vmId), false));
    }

    /**
     * This is a special case of searching the the links tree: we know that graphics_consoles must be the direct child
     * of the root.
     */
    private Optional<LinksTreeNode> findGraphicsConsoles(LinksTreeNode linksTree) {
        return findNode(linksTree, GRAPHICS_CONSOLES);
    }

    private Optional<LinksTreeNode> findCdroms(LinksTreeNode linksTree) {
        return findNode(linksTree, CD_ROMS);
    }

    private Optional<LinksTreeNode> findHostDevices(LinksTreeNode linksTree) {
        return findNode(linksTree, HOST_DEVICES);
    }

    private Optional<LinksTreeNode> findWatchdogs(LinksTreeNode linksTree) {
        return findNode(linksTree, WATCHDOGS);
    }

    private Optional<LinksTreeNode> findSnapshots(LinksTreeNode linksTree) {
        return findNode(linksTree, SNAPSHOTS);
    }

    protected InstanceType lookupInstance(Template template) {
        return getEntity(InstanceType.class,
                QueryType.GetInstanceType,
                new GetVmTemplateParameters(asGuid(template.getId())),
                "GetInstanceType");
    }

    protected VmTemplate lookupTemplate(Template template, Guid datacenterId) {
        if (template.isSetId()) {
            return getEntity(VmTemplate.class,
                    QueryType.GetVmTemplate,
                    new GetVmTemplateParameters(asGuid(template.getId())),
                    "GetVmTemplate");
        } else if (template.isSetName()) {
            GetVmTemplateParameters params = new GetVmTemplateParameters(template.getName());
            params.setDataCenterId(datacenterId);
            return getEntity(VmTemplate.class, QueryType.GetVmTemplate, params, "GetVmTemplate");
        }
        return null; // should never happen.
    }

    private Cluster lookupCluster(Guid id) {
        return getEntity(Cluster.class, QueryType.GetClusterById, new IdQueryParameters(id), "GetClusterById");
    }

    protected boolean namedCluster(Vm vm) {
        return vm.isSetCluster() && vm.getCluster().isSetName() && !vm.getCluster().isSetId();
    }

    protected Cluster getCluster(Vm vm) {
        if (namedCluster(vm)) {
            return isFiltered() ? lookupClusterByName(vm.getCluster().getName()) : getEntity(Cluster.class,
                    QueryType.GetClusterByName,
                    new NameQueryParameters(vm.getCluster().getName()),
                    "Cluster: name=" + vm.getCluster().getName());
        }

        return lookupCluster(asGuid(vm.getCluster().getId()));
    }

    public Cluster lookupClusterByName(String name) {
        return getEntity(Cluster.class, QueryType.GetClusterByName, new NameQueryParameters(name), "GetClusterByName");
    }

    protected Vm setVmOvfConfiguration (Vm model, org.ovirt.engine.core.common.businessentities.VM entity) {
        QueryReturnValue queryReturnValue =
                runQuery(QueryType.GetVmOvfByVmId,
                        new GetVmOvfByVmIdParameters(entity.getId(), entity.getDbGeneration(),
                                ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, OVF_AS_OVA, true, false)));

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
        BackendVmDeviceHelper.setConsoleDevice(this, model);
        BackendVmDeviceHelper.setVirtioScsiController(this, model);
        BackendVmDeviceHelper.setSoundcard(this, model);
        BackendVmDeviceHelper.setCertificateInfo(this, model);
        BackendVmDeviceHelper.setRngDevice(this, model);
        BackendVmDeviceHelper.setTpmDevice(this, model);
        setVmOvfConfiguration(model, entity);
        return model;
    }

    private List<String> getConsoleDevicesForEntity(Guid id) {
        return getEntity(List.class,
                QueryType.GetConsoleDevices,
                new IdQueryParameters(id),
                "GetConsoleDevices", true);
    }

    private boolean isVMDeviceTypeExist(Map<Guid, VmDevice> deviceMap, VmDeviceGeneralType deviceType) {
        return deviceMap != null && deviceMap.values().stream().anyMatch(d -> d.getType().equals(deviceType));
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

    protected void updateCpuPinningFields(VmStatic staticVm) {
        updateCpuPinningFields(staticVm, null);
    }

    protected void updateCpuPinningFields(VmStatic staticVm, CpuPinningPolicy previousPolicy) {
        // specified cpu pinning string without MANUAL policy
        if (!StringUtils.isEmpty(staticVm.getCpuPinning())
                && staticVm.getCpuPinningPolicy() == CpuPinningPolicy.NONE) {
            staticVm.setCpuPinningPolicy(CpuPinningPolicy.MANUAL);
        }

        // cpu pinning string removed
        if (StringUtils.isEmpty(staticVm.getCpuPinning())
                && staticVm.getCpuPinningPolicy() == CpuPinningPolicy.MANUAL
                && previousPolicy == CpuPinningPolicy.MANUAL) {
            staticVm.setCpuPinningPolicy(CpuPinningPolicy.NONE);
        }
    }
}
