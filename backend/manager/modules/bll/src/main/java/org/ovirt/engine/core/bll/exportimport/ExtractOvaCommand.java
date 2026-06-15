package org.ovirt.engine.core.bll.exportimport;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
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
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.EngineLocalConfig;
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
            prepareDisksBeforeExtract();
            List<String> diskPaths = prepareImages();
            String disksJson = prepareDisksJson(getDiskList(), diskPaths);
            boolean succeeded = runAnsibleImportOvaPlaybook(disksJson);
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

    protected void prepareDisksBeforeExtract() {
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

    private boolean runAnsibleImportOvaPlaybook(String disksJson) {
        long timeout = TimeUnit.MINUTES.toSeconds(
            EngineLocalConfig.getInstance().getInteger("ANSIBLE_PLAYBOOK_EXEC_DEFAULT_TIMEOUT"));
        AnsibleCommandConfig commandConfig = new AnsibleCommandConfig()
                .host(getVds())
                .variable("ovirt_import_ova_path", getParameters().getOvaPath())
                .variable("ovirt_import_ova_disks", disksJson)
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
     * @return JSON map keyed by OVA tar member name: {tarName: {path, format}}
     */
    private String prepareDisksJson(List<DiskImage> diskList, List<String> diskPaths) {
        List<String> tarNames = getParameters().getOvaTarNamesByIndex();
        if (tarNames == null || tarNames.size() != diskList.size()) {
            throw new EngineException(
                    EngineError.GeneralException,
                    "OVA extract: ovaTarNamesByIndex missing or size mismatch with disk list");
        }
        Map<String, Map<String, String>> entries = new LinkedHashMap<>();
        for (int i = 0; i < diskList.size(); i++) {
            Map<String, String> entry = new HashMap<>();
            entry.put("path", diskPaths.get(i));
            entry.put("format", diskList.get(i).getVolumeFormat() == VolumeFormat.COW ? "qcow2" : "raw");
            entries.put(tarNames.get(i), entry);
        }
        try {
            return encode(new ObjectMapper().writeValueAsString(entries));
        } catch (IOException e) {
            throw new RuntimeException("failed to serialize disk info");
        }
    }

    protected List<DiskImage> getDiskList() {
        return getParameters().getVmEntityType() == VmEntityType.TEMPLATE ?
                getVmTemplate().getDiskList()
                : getVm().getDiskList();
    }

    protected String prepareImagePath(DiskImage image) {
        VDSReturnValue vdsRetVal = imagesHandler.prepareImage(
                image.getStoragePoolId(),
                image.getStorageIds().get(0),
                image.getId(),
                image.getImageId(),
                getParameters().getProxyHostId());
        return ((PrepareImageReturn) vdsRetVal.getReturnValue()).getImagePath();
    }

    private void teardownImages() {
        getDiskList().forEach(this::teardownImage);
    }

    protected void teardownImage(DiskImage image) {
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
