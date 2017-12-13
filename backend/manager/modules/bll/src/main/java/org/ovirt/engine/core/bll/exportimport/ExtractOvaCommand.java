package org.ovirt.engine.core.bll.exportimport;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ConvertOvaParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandBuilder;
import org.ovirt.engine.core.common.utils.ansible.AnsibleConstants;
import org.ovirt.engine.core.common.utils.ansible.AnsibleExecutor;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnCode;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnValue;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.vdsbroker.vdsbroker.PrepareImageReturn;

@NonTransactiveCommandAttribute
public class ExtractOvaCommand<T extends ConvertOvaParameters> extends VmCommand<T> {

    public static final String IMPORT_OVA_LOG_DIRECTORY = "ova";

    @Inject
    private AnsibleExecutor ansibleExecutor;
    @Inject
    private VmHandler vmHandler;

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
    }

    @Override
    protected void executeVmCommand() {
        vmHandler.updateDisksFromDb(getVm());
        List<String> diskPaths = prepareImages();
        boolean succeeded = runAnsibleImportOvaPlaybook(diskPaths);
        teardownImages();
        setSucceeded(succeeded);
    }

    private boolean runAnsibleImportOvaPlaybook(List<String> diskPaths) {
        AnsibleCommandBuilder command = new AnsibleCommandBuilder()
                .hostnames(getVds().getHostName())
                .variables(
                    new Pair<>("ovirt_import_ova_path", getParameters().getOvaPath()),
                    new Pair<>("ovirt_import_ova_disks", String.join("+", diskPaths))
                )
                // /var/log/ovirt-engine/ova/ovirt-import-ova-ansible-{hostname}-{correlationid}-{timestamp}.log
                .logFileDirectory(IMPORT_OVA_LOG_DIRECTORY)
                .logFilePrefix("ovirt-import-ova-ansible")
                .logFileName(getVds().getHostName())
                .logFileSuffix(getCorrelationId())
                .playbook(AnsibleConstants.IMPORT_OVA_PLAYBOOK);

        boolean succeeded = false;
        AnsibleReturnValue ansibleReturnValue = null;
        try {
            ansibleReturnValue = ansibleExecutor.runCommand(command);
            succeeded = ansibleReturnValue.getAnsibleReturnCode() == AnsibleReturnCode.OK;
        } catch (IOException | InterruptedException e) {
            log.debug("Failed to extract OVA", e);
        }

        if (!succeeded) {
            log.error("Failed to extract OVA. Please check logs for more details: {}", command.logFile());
            return false;
        }

        return true;
    }

    /**
     * @return a list with the corresponding mounted paths
     */
    private List<String> prepareImages() {
        return getVm().getDiskList().stream()
                .map(this::prepareImage)
                .map(PrepareImageReturn::getImagePath)
                .collect(Collectors.toList());
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
        getVm().getDiskList().forEach(this::teardownImage);
    }

    private void teardownImage(DiskImage image) {
        imagesHandler.teardownImage(
                image.getStoragePoolId(),
                image.getStorageIds().get(0),
                image.getId(),
                image.getImageId(),
                getParameters().getProxyHostId());
    }
}
