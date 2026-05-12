package org.ovirt.engine.core.bll.exportimport;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.managedblock.ManagedBlockStorageCommandUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.ConvertOvaParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.utils.SecretValue;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandConfig;
import org.ovirt.engine.core.common.utils.ansible.AnsibleConstants;
import org.ovirt.engine.core.common.utils.ansible.AnsibleExecutor;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnCode;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnValue;
import org.ovirt.engine.core.common.utils.ansible.AnsibleRunnerClient;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.vdsbroker.vdsbroker.DeviceInfoReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.PrepareImageReturn;

import com.fasterxml.jackson.databind.ObjectMapper;

@NonTransactiveCommandAttribute
public class ExtractOvaCommand<T extends ConvertOvaParameters> extends VmCommand<T> {

    public static final String IMPORT_OVA_LOG_DIRECTORY = "ova";

    @Inject
    private AnsibleExecutor ansibleExecutor;
    @Inject
    private AnsibleRunnerClient runnerClient;
    @Inject
    private VmHandler vmHandler;
    @Inject
    private VmTemplateHandler templateHandler;
    @Inject
    private VmDao vmDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    private ManagedBlockStorageCommandUtil managedBlockStorageCommandUtil;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    public ExtractOvaCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        super.init();
        setVmName(getParameters().getVmName());
        setVdsId(getParameters().getProxyHostId());
        setClusterId(getParameters().getClusterId());
        setStoragePoolId(getParameters().getStoragePoolId());
        setStorageDomainId(getParameters().getStorageDomainId());
        if (getParameters().getVmEntityType() == VmEntityType.TEMPLATE) {
            setVmTemplateId(getParameters().getVmId());
        }
    }

    @Override
    protected void executeVmCommand() {
        try {
            updateDisksFromDb();
            if (isManagedBlockStorage()) {
                prepareManagedBlockDisksForAnsibleExtract();
            }
            List<String> diskPaths = prepareImages();
            String diskPathToFormat = prepareDiskPathToFormat(getDiskList(), diskPaths);
            boolean succeeded = runAnsibleImportOvaPlaybook(diskPathToFormat);
            teardownImages();
            if (!succeeded) {
                log.error("Failed to extract OVA file");
                setCommandStatus(CommandStatus.FAILED);
                return;
            }
            storeExternalData(runAnsibleOvaExternalDataPlaybook());
            setSucceeded(true);
        } catch (EngineException e) {
            log.error("Failed to extract OVA file", e);
            setCommandStatus(CommandStatus.FAILED);
        }
    }

    private void updateDisksFromDb() {
        if (getParameters().getVmEntityType() == VmEntityType.TEMPLATE) {
            VmTemplate vmt = getVmTemplate();
            if (vmt == null) {
                throw new EngineException(EngineError.GeneralException, "OVA extract: template entity not found");
            }
            templateHandler.updateDisksFromDb(vmt);
            if (vmt.getDiskList().isEmpty()) {
                templateHandler.addTemplateDisksFromDiskIds(vmt, getParameters().getTemplateDiskIdsForExtract());
            }
            if (vmt.getDiskList().isEmpty()) {
                int rawCount = diskDao.getAllForVm(vmt.getId()).size();
                log.error(
                        "OVA extract: no disks on template {} after reload (diskDao.getAllForVm returned {} row(s)); "
                                + "cannot build target paths for extract_ova.py",
                        vmt.getId(),
                        rawCount);
                throw new EngineException(
                        EngineError.GeneralException,
                        "OVA extract found no template disks in the engine database; check template id and disk attachment.");
            }
        } else {
            vmHandler.updateDisksFromDb(getVm());
        }
    }

    private boolean isManagedBlockStorage() {
        Guid poolId = getStoragePoolId();
        if (Guid.isNullOrEmpty(poolId)) {
            return false;
        }
        Guid sdId = getParameters().getStorageDomainId();
        if (!Guid.isNullOrEmpty(sdId)) {
            StorageDomain sd = storageDomainDao.getForStoragePool(sdId, poolId);
            if (sd != null && sd.getStorageType().isManagedBlockStorage()) {
                return true;
            }
        }
        for (DiskImage disk : getDiskList()) {
            List<Guid> storageIds = disk.getStorageIds();
            if (storageIds == null || storageIds.isEmpty()) {
                continue;
            }
            Guid diskSdId = storageIds.get(0);
            if (Guid.isNullOrEmpty(diskSdId)) {
                continue;
            }
            StorageDomain sd = storageDomainDao.getForStoragePool(diskSdId, poolId);
            if (sd != null && sd.getStorageType().isManagedBlockStorage()) {
                return true;
            }
        }
        return false;
    }

    private void prepareManagedBlockDisksForAnsibleExtract() {
        Map<Guid, Map<String, Object>> fromParent = getParameters().getPreAttachedManagedBlockDevicesByDiskId();
        if (fromParent != null) {
            for (DiskImage disk : getDiskList()) {
                if (disk instanceof ManagedBlockStorageDisk) {
                    Map<String, Object> dev = fromParent.get(disk.getId());
                    if (dev != null) {
                        ((ManagedBlockStorageDisk) disk).setDevice(new HashMap<>(dev));
                    }
                }
            }
        }
        List<ManagedBlockStorageDisk> mbsDisks =
                DisksFilter.filterManagedBlockStorageDisks(getDiskList());
        if (mbsDisks.isEmpty()) {
            return;
        }
        if (mbsDisks.stream().noneMatch(d -> d.getDevice() == null)) {
            return;
        }
        VDS host = getVds();
        if (host == null) {
            throw new EngineException(EngineError.GeneralException, "OVA extract requires a proxy host");
        }
        boolean parentSentDevices = fromParent != null && !fromParent.isEmpty();
        if (parentSentDevices) {
            throw new EngineException(
                    EngineError.StorageException,
                    "Managed-block import passed device metadata but disks still lack device after apply; "
                            + "check disk id alignment with the parent import.");
        }
        if (!managedBlockStorageCommandUtil.attachManagedBlockStorageDisksOnHost(
                mbsDisks,
                host,
                getParameters().getVmId())) {
            throw new EngineException(
                    EngineError.StorageException,
                    "Failed to connect or attach managed-block volumes on the OVA extract host");
        }
    }

    private String buildOvaImageMappingsYamlForAnsible() {
        Map<Guid, Guid> byDiskId = getParameters().getOvaSourceImageIdByDiskId();
        if (MapUtils.isNotEmpty(byDiskId)) {
            Map<Guid, Guid> currentImageIdToSource = new LinkedHashMap<>();
            for (DiskImage d : getDiskList()) {
                Guid sourceImageId = byDiskId.get(d.getId());
                if (sourceImageId != null) {
                    currentImageIdToSource.put(d.getImageId(), sourceImageId);
                }
            }
            if (!currentImageIdToSource.isEmpty()) {
                return currentImageIdToSource.entrySet()
                        .stream()
                        .map(e -> String
                                .format("\\\"%s\\\": \\\"%s\\\"", e.getValue().toString(), e.getKey().toString()))
                        .collect(Collectors.joining(", ", "{", "}"));
            }
        }
        Map<Guid, Guid> legacy = getParameters().getImageMappings();
        if (MapUtils.isEmpty(legacy)) {
            return "{}";
        }
        if (legacy.size() == 1 && getDiskList().size() == 1) {
            Map.Entry<Guid, Guid> e = legacy.entrySet().iterator().next();
            Guid sourceImageId = e.getValue();
            Guid currentImageId = getDiskList().get(0).getImageId();
            return "{" + String.format("\\\"%s\\\": \\\"%s\\\"",
                    sourceImageId.toString(),
                    currentImageId.toString()) + "}";
        }
        return legacy.entrySet()
                .stream()
                .map(e -> String.format("\\\"%s\\\": \\\"%s\\\"", e.getValue().toString(), e.getKey().toString()))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private boolean runAnsibleImportOvaPlaybook(String disksPathToFormat) {
        long timeout = TimeUnit.MINUTES.toSeconds(
            EngineLocalConfig.getInstance().getInteger("ANSIBLE_PLAYBOOK_EXEC_DEFAULT_TIMEOUT"));
        AnsibleCommandConfig commandConfig = new AnsibleCommandConfig()
                .host(getVds())
                .variable("ovirt_import_ova_path", getParameters().getOvaPath())
                .variable("ovirt_import_ova_disks", disksPathToFormat)
                .variable("ovirt_import_ova_image_mappings", buildOvaImageMappingsYamlForAnsible())
                .variable("ansible_timeout", timeout)
                // /var/log/ovirt-engine/ova/ovirt-import-ova-ansible-{hostname}-{correlationid}-{timestamp}.log
                .logFileDirectory(IMPORT_OVA_LOG_DIRECTORY)
                .logFilePrefix("ovirt-import-ova-ansible")
                .logFileName(getVds().getHostName())
                .playAction("Import OVA")
                .playbook(AnsibleConstants.IMPORT_OVA_PLAYBOOK);

        AnsibleReturnValue ansibleReturnValue = ansibleExecutor.runCommand(commandConfig);
        boolean succeeded = ansibleReturnValue.getAnsibleReturnCode() == AnsibleReturnCode.OK;
        if (!succeeded) {
            log.error("Failed to extract OVA. Please check logs for more details: {}", ansibleReturnValue.getLogFile());
            return false;
        }

        return true;
    }

    private String runAnsibleOvaExternalDataPlaybook() {
        AnsibleCommandConfig command = new AnsibleCommandConfig()
                .host(getVds())
                .variable("ovirt_ova_path", getParameters().getOvaPath())
                // /var/log/ovirt-engine/ova/ovirt-ova-external-data-ansible-{hostname}-{timestamp}.log
                .logFileDirectory(ExtractOvaCommand.IMPORT_OVA_LOG_DIRECTORY)
                .logFilePrefix("ovirt-ova-external-data-ansible")
                .logFileName(getVds().getHostName())
                .playAction("Get external data from OVA")
                .playbook(AnsibleConstants.OVA_EXTERNAL_DATA_PLAYBOOK);

        StringBuilder stdout = new StringBuilder();
        AnsibleReturnValue ansibleReturnValue = ansibleExecutor.runCommand(
                command,
                log,
                (eventName, eventUrl) -> stdout.append(runnerClient.getCommandStdout(eventUrl))
        );

        boolean succeeded = ansibleReturnValue.getAnsibleReturnCode() == AnsibleReturnCode.OK;
        if (!succeeded) {
            log.error("Failed to get external data from OVA: {}", ansibleReturnValue.getStderr());
            throw new EngineException(EngineError.GeneralException, "Failed to get external data from OVA");
        }

        return stdout.toString();
    }

    private String encode(String str) {
        // replace " characters with \"
        return str.replaceAll("\"", "\\\\\"");
    }

    /**
     * @return a list with the corresponding mounted paths
     */
    private List<String> prepareImages() {
        return getDiskList().stream()
                .map(this::prepareImagePath)
                .collect(Collectors.toList());
    }

    /**
     * @return a json with the corresponding mounted disks paths and formats
     */
    private String prepareDiskPathToFormat(List<DiskImage> diskList, List<String> diskPaths) {
        Map<String, String> diskPathToFormat = IntStream.range(0, diskList.size())
                .boxed()
                .collect(Collectors.toMap(i -> diskPaths.get(i),
                        i -> diskList.get(i).getVolumeFormat() == VolumeFormat.COW ? "qcow2" : "raw"));
        Map<String, String> diskPathToImageId = IntStream.range(0, diskList.size())
                .boxed()
                .collect(Collectors.toMap(i -> diskPaths.get(i),
                        i -> diskList.get(i).getImageId().toString()));
        Map<String, Object> spec = new HashMap<>();
        spec.put("pathToFormat", diskPathToFormat);
        spec.put("pathToImageId", diskPathToImageId);
        String json;
        try {
            json = new ObjectMapper().writeValueAsString(spec);
        } catch (IOException e) {
            throw new RuntimeException("failed to serialize disk info");
        }
        return encode(json);
    }

    private List<DiskImage> getDiskList() {
        return getParameters().getVmEntityType() == VmEntityType.TEMPLATE ?
                getVmTemplate().getDiskList()
                : getVm().getDiskList();
    }

    private String prepareImagePath(DiskImage image) {
        if (image instanceof ManagedBlockStorageDisk) {
            return managedBlockImagePath((ManagedBlockStorageDisk) image);
        }
        VDSReturnValue vdsRetVal = imagesHandler.prepareImage(
                image.getStoragePoolId(),
                image.getStorageIds().get(0),
                image.getId(),
                image.getImageId(),
                getParameters().getProxyHostId());
        return ((PrepareImageReturn) vdsRetVal.getReturnValue()).getImagePath();
    }

    private String managedBlockImagePath(ManagedBlockStorageDisk disk) {
        Map<String, Object> device = disk.getDevice();
        if (device == null) {
            throw new EngineException(
                    EngineError.StorageException,
                    "Managed-block volume has no device on the proxy host; attach volumes before OVA extract.");
        }

        String path = (String) device.get(DeviceInfoReturn.MANAGED_PATH);
        if (StringUtils.isEmpty(path)) {
            path = (String) device.get(DeviceInfoReturn.PATH);
        }
        if (StringUtils.isEmpty(path)) {
            throw new EngineException(
                    EngineError.StorageException,
                    "Managed-block device path missing in volume metadata after attach.");
        }
        return path;
    }

    private void teardownImages() {
        getDiskList().forEach(this::teardownImage);
    }

    private void teardownImage(DiskImage image) {
        if (image instanceof ManagedBlockStorageDisk) {
            Guid proxy = getParameters().getProxyHostId();
            if (proxy != null && !Guid.isNullOrEmpty(proxy)) {
                managedBlockStorageCommandUtil.disconnectManagedBlockStorageDeviceFromHost(image, proxy);
            }
            return;
        }
        imagesHandler.teardownImage(
                image.getStoragePoolId(),
                image.getStorageIds().get(0),
                image.getId(),
                image.getImageId(),
                getParameters().getProxyHostId());
    }

    private void storeExternalData(String stdout) {
        Map<String, SecretValue<String>> externalData = Arrays.stream(stdout.trim().split(";"))
                .filter(s -> !StringUtils.isEmpty(s)).map(s -> s.split("=", 2))
                .collect(Collectors.toMap(part -> part[0], part -> new SecretValue<String>(part[1])));
        SecretValue<String> tpmData = externalData.get("tpm");
        if (tpmData != null && !StringUtils.isEmpty(tpmData.getValue())) {
            vmDao.updateTpmData(getVmId(), tpmData, null);
        }
        SecretValue<String> nvramData = externalData.get("nvram");
        if (nvramData != null && !StringUtils.isEmpty(nvramData.getValue())) {
            vmDao.updateNvramData(getVmId(), nvramData, null);
        }
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

}
