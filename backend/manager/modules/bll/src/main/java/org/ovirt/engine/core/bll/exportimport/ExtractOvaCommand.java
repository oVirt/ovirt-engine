package org.ovirt.engine.core.bll.exportimport;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.ConvertOvaParameters;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandConfig;
import org.ovirt.engine.core.common.utils.ansible.AnsibleConstants;
import org.ovirt.engine.core.common.utils.ansible.AnsibleExecutor;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnCode;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnValue;
import org.ovirt.engine.core.common.utils.ansible.AnsibleRunnerHttpClient;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.PrepareImageReturn;

import com.fasterxml.jackson.databind.ObjectMapper;

@NonTransactiveCommandAttribute
public class ExtractOvaCommand<T extends ConvertOvaParameters> extends VmCommand<T> {

    public static final String IMPORT_OVA_LOG_DIRECTORY = "ova";

    @Inject
    private AnsibleExecutor ansibleExecutor;
    @Inject
    private AnsibleRunnerHttpClient runnerClient;
    @Inject
    private VmHandler vmHandler;
    @Inject
    private VmTemplateHandler templateHandler;
    @Inject
    private VmDao vmDao;
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
        } catch(EngineException e) {
            log.error("Failed to extract OVA file", e);
            setCommandStatus(CommandStatus.FAILED);
        }
    }

    private void updateDisksFromDb() {
        if (getParameters().getVmEntityType() == VmEntityType.TEMPLATE) {
            templateHandler.updateDisksFromDb(getVmTemplate());
        } else {
            vmHandler.updateDisksFromDb(getVm());
        }
    }

    private Map<Guid, Guid> getImageMappings() {
        return getParameters().getImageMappings() != null ?
                getParameters().getImageMappings()
                : Collections.emptyMap();
    }

    private boolean runAnsibleImportOvaPlaybook(String disksPathToFormat) {
        AnsibleCommandConfig commandConfig = new AnsibleCommandConfig()
                .hosts(getVds())
                .variable("ovirt_import_ova_path", getParameters().getOvaPath())
                .variable("ovirt_import_ova_disks", disksPathToFormat)
                .variable("ovirt_import_ova_image_mappings",
                        getImageMappings().entrySet()
                                .stream()
                                .map(e -> String
                                        .format("\\\"%s\\\": \\\"%s\\\"", e.getValue().toString(), e.getKey().toString()))
                                .collect(Collectors.joining(", ", "{", "}")))
                // /var/log/ovirt-engine/ova/ovirt-import-ova-ansible-{hostname}-{correlationid}-{timestamp}.log
                .logFileDirectory(IMPORT_OVA_LOG_DIRECTORY)
                .logFilePrefix("ovirt-import-ova-ansible")
                .logFileName(getVds().getHostName())
                .playAction("Import OVA")
                .playbook(AnsibleConstants.IMPORT_OVA_PLAYBOOK);

        AnsibleReturnValue ansibleReturnValue  = ansibleExecutor.runCommand(commandConfig);
        boolean succeeded = ansibleReturnValue.getAnsibleReturnCode() == AnsibleReturnCode.OK;
        if (!succeeded) {
            log.error("Failed to extract OVA. Please check logs for more details: {}", ansibleReturnValue.getLogFile());
            return false;
        }

        return true;
    }

    private String runAnsibleOvaExternalDataPlaybook() {
        AnsibleCommandConfig command = new AnsibleCommandConfig()
                .hosts(getVds())
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
                .map(this::prepareImage)
                .map(PrepareImageReturn::getImagePath)
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
        String json;
        try {
            json = new ObjectMapper().writeValueAsString(diskPathToFormat);
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

    private PrepareImageReturn prepareImage(DiskImage image) {
        VDSReturnValue vdsRetVal = imagesHandler.prepareImage(
                image.getStoragePoolId(),
                image.getStorageIds().get(0),
                image.getId(),
                image.getImageId(),
                getParameters().getProxyHostId());
        return (PrepareImageReturn) vdsRetVal.getReturnValue();
    }

    private void teardownImages() {
        getDiskList().forEach(this::teardownImage);
    }

    private void teardownImage(DiskImage image) {
        imagesHandler.teardownImage(
                image.getStoragePoolId(),
                image.getStorageIds().get(0),
                image.getId(),
                image.getImageId(),
                getParameters().getProxyHostId());
    }

    private void storeExternalData(String stdout) {
        Map<String, String> externalData = Arrays.stream(stdout.trim().split(";"))
                .filter(s -> !StringUtils.isEmpty(s))
                .map(s -> s.split("=", 2))
                .collect(Collectors.toMap(part -> part[0], part -> part[1]));
        String tpmData = externalData.get("tpm");
        if (!StringUtils.isEmpty(tpmData)) {
            vmDao.updateTpmData(getVmId(), tpmData, null);
        }
        String nvramData = externalData.get("nvram");
        if (!StringUtils.isEmpty(nvramData)) {
            vmDao.updateNvramData(getVmId(), nvramData, null);
        }
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

}
