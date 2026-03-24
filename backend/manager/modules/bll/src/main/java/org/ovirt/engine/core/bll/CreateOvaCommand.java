package org.ovirt.engine.core.bll;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.storage.disk.DiskHandler;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AnsibleCommandParameters;
import org.ovirt.engine.core.common.action.AnsibleImageMeasureCommandParameters;
import org.ovirt.engine.core.common.action.ConnectManagedBlockStorageDeviceCommandParameters;
import org.ovirt.engine.core.common.action.CreateOvaParameters;
import org.ovirt.engine.core.common.action.DisconnectManagedBlockStorageDeviceParameters;
import org.ovirt.engine.core.common.action.VmExternalDataKind;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SecretValue;
import org.ovirt.engine.core.common.utils.managedblock.ManagedBlockCommandParameters;
import org.ovirt.engine.core.common.utils.managedblock.ManagedBlockExecutor;
import org.ovirt.engine.core.common.utils.managedblock.ManagedBlockReturnValue;
import org.ovirt.engine.core.common.vdscommands.AttachManagedBlockStorageVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ManagedBlockStorageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.DeviceInfoReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.PrepareImageReturn;

import com.fasterxml.jackson.databind.ObjectMapper;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class CreateOvaCommand<T extends CreateOvaParameters> extends CommandBase<T> implements SerialChildExecutingCommand {

    @Inject
    private OvfManager ovfManager;
    @Inject
    protected AuditLogDirector auditLogDirector;
    @Inject
    private VmHandler vmHandler;
    @Inject
    private DiskHandler diskHandler;
    @Inject
    private VmDeviceUtils vmDeviceUtils;
    @Inject
    private ImagesHandler imagesHandler;
    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private ManagedBlockStorageDao managedBlockStorageDao;
    @Inject
    private ManagedBlockExecutor managedBlockExecutor;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private VDSBrokerFrontend vdsBrokerFrontend;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    public static final String CREATE_OVA_LOG_DIRECTORY = "ova";
    public static final int TAR_BLOCK_SIZE = 512;

    public CreateOvaCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        super.init();
        setVdsId(getParameters().getProxyHostId());
    }

    @Override
    protected void executeCommand() {
        List<DiskImage> disks = getParameters().getDisks();
        Map<Guid, String> diskIdToPath = prepareImages(disks);
        getParameters().setDiskIdToPath(diskIdToPath);
        fillDiskApparentSize(disks, diskIdToPath);
        setSucceeded(true);
    }

    private boolean isManagedBlockDisk(DiskImage image) {
        return storageDomainDao.get(image.getStorageIds().get(0)).getStorageType() == StorageType.MANAGED_BLOCK_STORAGE;
    }

    private Pair<String, Version> createOvf(Collection<DiskImage> disks) {
        switch (getParameters().getEntityType()) {
            case TEMPLATE:
                VmTemplate template = vmTemplateDao.get(getParameters().getEntityId());
                vmHandler.updateVmInitFromDB(template, true);
                vmDeviceUtils.setVmDevices(template);
                template.setVmExternalData(getVmExternalData());
                List<VmNetworkInterface> interfaces = vmNetworkInterfaceDao.getAllForTemplate(template.getId());
                template.setInterfaces(interfaces);
                FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(template);
                fullEntityOvfData.setDiskImages(new ArrayList<>(disks));
                fullEntityOvfData.setInterfaces(interfaces);
                return new Pair<>(ovfManager.exportOva(template, fullEntityOvfData, template.getCompatibilityVersion()),
                        template.getCompatibilityVersion());

            default:
                VM vm = vmDao.get(getParameters().getEntityId());
                vmHandler.updateVmInitFromDB(vm.getStaticData(), true);
                vmHandler.updateNumaNodesFromDb(vm);
                vm.setVmExternalData(getVmExternalData());
                interfaces = vmNetworkInterfaceDao.getAllForVm(vm.getId());
                vm.setInterfaces(interfaces);
                vmDeviceUtils.setVmDevices(vm.getStaticData());
                fullEntityOvfData = new FullEntityOvfData(vm);
                fullEntityOvfData.setDiskImages(new ArrayList<>(disks));
                fullEntityOvfData.setInterfaces(interfaces);
                return new Pair<>(ovfManager.exportOva(vm, fullEntityOvfData, vm.getCompatibilityVersion()),
                        vm.getCompatibilityVersion());
        }
    }

    private Map<Guid, String> prepareImages(Collection<DiskImage> disks) {
        return disks.stream()
                .collect(Collectors.toMap(
                        DiskImage::getId,
                        image -> isManagedBlockDisk(image)
                                ? prepareManagedBlockImage(image)
                                : prepareImage(image).getImagePath()));
    }

    private PrepareImageReturn prepareImage(DiskImage image) {
        VDSReturnValue vdsRetVal = imagesHandler.prepareImage(
                image.getStoragePoolId(),
                image.getStorageIds().get(0),
                image.getId(),
                image.getImageId(),
                getParameters().getProxyHostId());
        return (PrepareImageReturn) vdsRetVal.getReturnValue();
    }

    private String prepareManagedBlockImage(DiskImage image) {
        Guid sdId = image.getStorageIds().get(0);
        ManagedBlockStorage mbs = managedBlockStorageDao.get(sdId);

        Guid volumeId;
        if (image.getActive()) {
            volumeId = image.getImageId();
        } else {
            volumeId = createVolumeFromSnapshot(mbs, image.getId(), image.getImageId());
        }

        String devicePath;
        try {
            devicePath = connectAndAttachManagedBlockVolume(sdId, volumeId);
        } catch (EngineException e) {
            if (!image.getActive()) {
                deleteManagedBlockVolume(mbs, volumeId);
            }
            throw e;
        }

        if (!image.getActive()) {
            getParameters().getMbsSnapshotImageToTempVolume().put(image.getImageId(), volumeId);
            persistCommandIfNeeded();
        }

        return devicePath;
    }

    @SuppressWarnings("unchecked")
    private String connectAndAttachManagedBlockVolume(Guid sdId, Guid volumeId) {
        ConnectManagedBlockStorageDeviceCommandParameters connectParams =
                new ConnectManagedBlockStorageDeviceCommandParameters();
        connectParams.setStorageDomainId(sdId);
        connectParams.setDiskId(volumeId);
        connectParams.setConnectorInfo(getVds().getConnectorInfo());
        ActionReturnValue connectResult =
                runInternalAction(ActionType.ConnectManagedBlockStorageDevice, connectParams);
        if (!connectResult.getSucceeded()) {
            throw new EngineException(EngineError.GeneralException,
                    "Failed to connect managed block storage volume " + volumeId);
        }

        try {
            Map<String, Object> connectionInfo =
                    (Map<String, Object>) connectResult.getActionReturnValue();

            AttachManagedBlockStorageVolumeVDSCommandParameters attachParams =
                    new AttachManagedBlockStorageVolumeVDSCommandParameters(
                        getVds(), connectionInfo, sdId);
            attachParams.setVolumeId(volumeId);
            VDSReturnValue attachResult = vdsBrokerFrontend.runVdsCommand(
                    VDSCommandType.AttachManagedBlockStorageVolume, attachParams);
            if (!attachResult.getSucceeded()) {
                throw new EngineException(EngineError.GeneralException,
                    "Failed to attach managed block storage volume " + volumeId);
            }

            Map<String, Object> deviceInfo = (Map<String, Object>) attachResult.getReturnValue();
            String devicePath = (String) deviceInfo.get(DeviceInfoReturn.MANAGED_PATH);
            if (devicePath == null) {
                devicePath = (String) deviceInfo.get(DeviceInfoReturn.PATH);
            }
            if (devicePath == null) {
                throw new EngineException(EngineError.GeneralException,
                    "Failed to get device path for managed block storage volume " + volumeId);
            }
            return devicePath;
        } catch (RuntimeException e) {
            disconnectManagedBlockVolume(sdId, volumeId);
            throw e;
        }
    }

    private void disconnectManagedBlockVolume(Guid sdId, Guid volumeId) {
        runInternalAction(ActionType.DisconnectManagedBlockStorageDevice,
                new DisconnectManagedBlockStorageDeviceParameters(sdId, null, volumeId, getVdsId()));
    }

    private Guid createVolumeFromSnapshot(ManagedBlockStorage mbs, Guid mainVolumeId, Guid snapshotId) {
        List<String> extraParams = new ArrayList<>();
        extraParams.add(mainVolumeId.toString());
        extraParams.add(snapshotId.toString());
        try {
            ManagedBlockCommandParameters params = new ManagedBlockCommandParameters(
                JsonHelper.mapToJson(mbs.getAllDriverOptions(), false),
                extraParams,
                getCorrelationId());
            ManagedBlockReturnValue result = managedBlockExecutor.runCommand(
                ManagedBlockExecutor.ManagedBlockCommand.CREATE_VOLUME_FROM_SNAPSHOT, params);
            if (!result.getSucceed()) {
                throw new EngineException(EngineError.GeneralException,
                    "Managed Block CREATE_VOLUME_FROM_SNAPSHOT command failed for snapshot " + snapshotId);
            }
            return Guid.createGuidFromString(result.getOutput());
        } catch (EngineException e) {
            throw e;
        } catch (Exception e) {
            throw new EngineException(EngineError.GeneralException, e, true);
        }
    }

    private void deleteManagedBlockVolume(ManagedBlockStorage mbs, Guid volumeId) {
        List<String> extraParams = new ArrayList<>();
        extraParams.add(volumeId.toString());
        try {
            ManagedBlockCommandParameters params = new ManagedBlockCommandParameters(
                JsonHelper.mapToJson(mbs.getAllDriverOptions(), false),
                extraParams,
                getCorrelationId());
            managedBlockExecutor.runCommand(
                ManagedBlockExecutor.ManagedBlockCommand.DELETE_VOLUME, params);
        } catch (Exception e) {
            log.error("Exception while deleting managed block volume " + volumeId, e);
        }
    }

    private void teardownImage(DiskImage image) {
        imagesHandler.teardownImage(
                image.getStoragePoolId(),
                image.getStorageIds().get(0),
                image.getId(),
                image.getImageId(),
                getParameters().getProxyHostId());
    }

    private void teardownManagedBlockImage(DiskImage image) {
        Guid sdId = image.getStorageIds().get(0);
        Guid volumeId;
        if (image.getActive()) {
            volumeId = image.getImageId();
        } else {
            volumeId = getParameters().getMbsSnapshotImageToTempVolume().get(image.getImageId());
            if (volumeId == null) {
                log.warn("No temp volume found for managed block storage snapshot image {}, skipping teardown",
                        image.getImageId());
                return;
            }
        }

        disconnectManagedBlockVolume(sdId, volumeId);

        if (!image.getActive()) {
            getParameters().getMbsSnapshotImageToTempVolume().remove(image.getImageId());
            deleteManagedBlockVolume(managedBlockStorageDao.get(sdId), volumeId);
        }
    }

    private void fillDiskApparentSize(List<DiskImage> disks, Map<Guid, String> diskIdToPath) {
        disks.forEach(disk -> runAnsibleImageMeasurePlaybook(diskIdToPath.get(disk.getId()), disk.getId()));
    }

    private void runAnsibleImageMeasurePlaybook(String path, Guid diskId) {
        ActionReturnValue actionReturnValue = runInternalAction(ActionType.AnsibleImageMeasure,
                createImageMeasureParameters(path, diskId),
                ExecutionHandler.createDefaultContextForTasks(getContext()));
        if (!actionReturnValue.getSucceeded()) {
            log.error("Failed to start Ansible Image Measure playbook");
            throw new EngineException(EngineError.GeneralException, "Failed to measure image");
        }
    }

    private AnsibleCommandParameters createImageMeasureParameters(String path, Guid diskId) {
        AnsibleImageMeasureCommandParameters params = new AnsibleImageMeasureCommandParameters();
        params.setHostId(getVdsId());
        params.setDiskId(diskId);
        params.setDisks(getParameters().getDisks());
        params.setPlayAction("Image measure");
        params.setParentCommand(getActionType());
        params.setParentParameters(getParameters());
        params.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        Map<String, Object> vars = new HashMap<>();
        vars.put("image_path", path);
        params.setVariables(vars);
        return params;
    }

    private AnsibleCommandParameters createPackOvaParameters(String ovf,
            Collection<DiskImage> disks,
            Map<Guid, String> diskIdToPath,
            SecretValue<String> tpmData,
            SecretValue<String> nvramData,
            Version compatibilityVersion) {
        String encodedOvf = encode(ovf);
        AnsibleCommandParameters params = new AnsibleCommandParameters();
        params.setHostId(getVdsId());
        params.setPlayAction("Pack OVA");
        params.setParentCommand(getActionType());
        params.setParentParameters(getParameters());
        params.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        Map<String, Object> vars = new HashMap<>();
        vars.put("target_directory", getParameters().getDirectory());
        vars.put("entity_type", getParameters().getEntityType().name().toLowerCase());
        vars.put("ova_size", String.valueOf(calcOvaSize(disks, tpmData, nvramData, encodedOvf)));
        vars.put("ova_name", getParameters().getName());
        vars.put("ovirt_ova_pack_ovf", encodedOvf);
        vars.put("ovirt_ova_pack_disks", genDiskParameters(disks, diskIdToPath));
        if (!SecretValue.isNull(tpmData) && !tpmData.getValue().isEmpty()) {
            vars.put("ovirt_ova_pack_tpm", tpmData.getValue());
        }
        if (!SecretValue.isNull(nvramData) && !nvramData.getValue().isEmpty()) {
            vars.put("ovirt_ova_pack_nvram", nvramData.getValue());
        }
        vars.put("ovirt_ova_pack_padding", Boolean.toString(compatibilityVersion.greater(Version.v4_6)));
        params.setVariables(vars);
        return params;
    }

    private void packOva() {
        List<DiskImage> disks = getParameters().getDisks();
        disks.forEach(this::updateDiskVmElementFromDb);
        Pair<String, Version> ovf = createOvf(disks);
        log.debug("Exporting OVF: {}", ovf.getFirst());
        ActionReturnValue actionReturnValue = runInternalAction(ActionType.AnsiblePackOva,
                createPackOvaParameters(ovf.getFirst(), disks, getParameters().getDiskIdToPath(), getTpmData(),
                        getNvramData(), ovf.getSecond()),
                ExecutionHandler.createDefaultContextForTasks(getContext()));
        if (!actionReturnValue.getSucceeded()) {
            log.error("Failed to start Ansible Pack OVA playbook");
            throw new EngineException(EngineError.GeneralException, "Failed to pack ova");
        }
        setSucceeded(true);
    }

    private SecretValue<String> getTpmData() {
        var tpmDataAndHash = vmDao.getTpmData(getParameters().getEntityId());
        return tpmDataAndHash != null ? tpmDataAndHash.getFirst() : null;
    }

    private SecretValue<String> getNvramData() {
        var nvramDataAndHash = vmDao.getNvramData(getParameters().getEntityId());
        return nvramDataAndHash != null ? nvramDataAndHash.getFirst() : null;
    }

    private Map<VmExternalDataKind, SecretValue<String>> getVmExternalData() {
        Map<VmExternalDataKind, SecretValue<String>> externalData = new HashMap<>();
        SecretValue<String> tpmData = getTpmData();
        if (tpmData != null && !StringUtils.isEmpty(tpmData.getValue())) {
            externalData.put(VmExternalDataKind.TPM, tpmData);
        }
        SecretValue<String> nvramData = getNvramData();
        if (nvramData != null && !StringUtils.isEmpty(nvramData.getValue())) {
            externalData.put(VmExternalDataKind.NVRAM, nvramData);
        }
        return externalData;
    }

    private void updateDiskVmElementFromDb(DiskImage diskImage) {
        diskHandler.updateDiskVmElementFromDb(diskImage, getParameters().getEntityId());
    }

    private long calcOvaSize(Collection<DiskImage> disks, SecretValue<String> tpmData, SecretValue<String> nvramData,
            String ovf) {
        // 1 block for the OVF, 1 block per-disk and 2 null-blocks at the end
        return TAR_BLOCK_SIZE * (1 + disks.size() + 2) + blockAlignedSize(ovf.length())
                + (SecretValue.isNull(tpmData) ? 0 : blockAlignedSize(tpmData.getValue().length()))
                + (SecretValue.isNull(nvramData) ? 0 : blockAlignedSize(nvramData.getValue().length()))
                + disks.stream().mapToLong(DiskImage::getActualSizeInBytes).sum();
    }

    private long blockAlignedSize(long size) {
        return (long) Math.ceil(size / (TAR_BLOCK_SIZE * 1.0)) * TAR_BLOCK_SIZE;
    }

    private String encode(String str) {
        // replace " characters with \"
        return str.replaceAll("\"", "\\\\\"");
    }

    private String genDiskParameters(Collection<DiskImage> disks, Map<Guid, String> diskIdToPath) {
        var diskPathToInfo = disks.stream()
                .collect(Collectors.toMap(
                        disk -> diskIdToPath.get(disk.getId()),
                        disk -> Map.of(
                            "size", disk.getActualSizeInBytes(),
                            "name", disk.getImageId().toString())));
        String json;
        try {
            json = new ObjectMapper().writeValueAsString(diskPathToInfo);
        } catch (IOException e) {
            throw new RuntimeException("failed to serialize disk info");
        }
        return encode(json);
    }

    private void teardown() {
        boolean canTeardownNonMBS = getParameters().getEntityType() == VmEntityType.TEMPLATE
                || vmDao.get(getParameters().getEntityId()).isDown();
        for (DiskImage image : getParameters().getDisks()) {
            if (isManagedBlockDisk(image)) {
                teardownManagedBlockImage(image);
            } else if (canTeardownNonMBS) {
                teardownImage(image);
            }
        }
        setSucceeded(true);
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        switch (getParameters().getPhase()) {
            case MEASURE:
                getParameters().setPhase(CreateOvaParameters.Phase.PACK_OVA);
                break;

            case PACK_OVA:
                getParameters().setPhase(CreateOvaParameters.Phase.TEARDOWN);
                break;

            case TEARDOWN:
                return false;

            default:
        }

        persistCommandIfNeeded();
        executeNextOperation();
        return true;
    }

    private void executeNextOperation() {
        switch (getParameters().getPhase()) {
            case MEASURE:
                fillDiskApparentSize(getParameters().getDisks(), getParameters().getDiskIdToPath());
                break;

            case PACK_OVA:
                packOva();
                break;

            case TEARDOWN:
                teardown();
                break;
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }
}
