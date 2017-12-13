package org.ovirt.engine.core.bll;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.action.CreateOvaParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandBuilder;
import org.ovirt.engine.core.common.utils.ansible.AnsibleConstants;
import org.ovirt.engine.core.common.utils.ansible.AnsibleExecutor;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnCode;
import org.ovirt.engine.core.common.vdscommands.GetVolumeInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.PrepareImageReturn;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class CreateOvaCommand<T extends CreateOvaParameters> extends CommandBase<T> {

    @Inject
    private OvfManager ovfManager;
    @Inject
    private AnsibleExecutor ansibleExecutor;
    @Inject
    protected AuditLogDirector auditLogDirector;
    @Inject
    private VdsCommandsHelper vdsCommandsHelper;
    @Inject
    private VmHandler vmHandler;
    @Inject
    private VmDeviceUtils vmDeviceUtils;
    @Inject
    private ImagesHandler imagesHandler;

    public static final String CREATE_OVA_LOG_DIRECTORY = "ova";

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
        Map<DiskImage, DiskImage> diskMappings = getParameters().getDiskInfoDestinationMap();
        Collection<DiskImage> disks = diskMappings.values();
        Map<Guid, String> diskIdToPath = prepareImages(disks);
        fillDiskApparentSize(disks);
        VM vm = getParameters().getVm();
        vmHandler.updateNetworkInterfacesFromDb(vm);
        vmHandler.updateVmInitFromDB(vm.getStaticData(), true);
        vmDeviceUtils.setVmDevices(vm.getStaticData());
        fixDiskDevices(vm, diskMappings);
        FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(vm);
        fullEntityOvfData.setDiskImages(new ArrayList<>(disks));
        fullEntityOvfData.setInterfaces(vm.getInterfaces());
        String ovf = ovfManager.exportOva(vm, fullEntityOvfData, vm.getCompatibilityVersion());
        log.debug("Exporting OVF: {}", ovf);
        boolean succeeded = runAnsiblePackOvaPlaybook(vm.getName(), ovf, disks, diskIdToPath);
        teardownImages(disks);
        setSucceeded(succeeded);
    }

    private void fixDiskDevices(VM vm, Map<DiskImage, DiskImage> diskMappings) {
        Map<Guid, Guid> diskIdMappings = new HashMap<>();
        diskMappings.forEach((source, destination) -> diskIdMappings.put(source.getId(), destination.getId()));

        List<VmDevice> diskDevices = vm.getStaticData().getManagedDeviceMap().values().stream()
                .filter(VmDeviceCommonUtils::isDisk)
                .collect(Collectors.toList());
        diskDevices.forEach(diskDevice -> {
            Guid sourceDiskId = diskDevice.getDeviceId();
            Guid destinationDiskId = diskIdMappings.get(sourceDiskId);
            if (destinationDiskId != null) {
                vm.getStaticData().getManagedDeviceMap().remove(sourceDiskId);
                diskDevice.setDeviceId(destinationDiskId);
                vm.getStaticData().getManagedDeviceMap().put(destinationDiskId, diskDevice);
            }
        });
    }

    private Map<Guid, String> prepareImages(Collection<DiskImage> disks) {
        return disks.stream()
                .collect(Collectors.toMap(
                        DiskImage::getId,
                        image -> prepareImage(image).getImagePath()));
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

    private void teardownImages(Collection<DiskImage> disks) {
        disks.forEach(this::teardownImage);
    }

    private void teardownImage(DiskImage image) {
        imagesHandler.teardownImage(
                image.getStoragePoolId(),
                image.getStorageIds().get(0),
                image.getId(),
                image.getImageId(),
                getParameters().getProxyHostId());
    }

    private void fillDiskApparentSize(Collection<DiskImage> disks) {
        disks.forEach(destination -> {
            VDSReturnValue vdsReturnValue = vdsCommandsHelper.runVdsCommandWithFailover(
                    VDSCommandType.GetVolumeInfo,
                    new GetVolumeInfoVDSCommandParameters(
                            destination.getStoragePoolId(),
                            destination.getStorageIds().get(0),
                            destination.getId(),
                            destination.getImageId()), destination.getStoragePoolId(), null);
            if (vdsReturnValue != null && vdsReturnValue.getSucceeded()) {
                DiskImage fromVdsm = (DiskImage) vdsReturnValue.getReturnValue();
                destination.setActualSizeInBytes(fromVdsm.getApparentSizeInBytes());
            }
        });
    }

    private boolean runAnsiblePackOvaPlaybook(String vmName, String ovf, Collection<DiskImage> disks, Map<Guid, String> diskIdToPath) {
        AnsibleCommandBuilder command = new AnsibleCommandBuilder()
                .hostnames(getVds().getHostName())
                .variables(
                    new Pair<>("target_directory", getParameters().getDirectory()),
                    new Pair<>("ova_name", getParameters().getName()),
                    new Pair<>("ovirt_ova_pack_ovf", genOvfParameter(ovf)),
                    new Pair<>("ovirt_ova_pack_disks", genDiskParameters(disks, diskIdToPath))
                )
                // /var/log/ovirt-engine/ova/ovirt-export-ova-ansible-{hostname}-{correlationid}-{timestamp}.log
                .logFileDirectory(CREATE_OVA_LOG_DIRECTORY)
                .logFilePrefix("ovirt-export-ova-ansible")
                .logFileName(getVds().getHostName())
                .logFileSuffix(getCorrelationId())
                .playbook(AnsibleConstants.EXPORT_OVA_PLAYBOOK);

        boolean succeeded = false;
        try {
            succeeded = ansibleExecutor.runCommand(command).getAnsibleReturnCode() == AnsibleReturnCode.OK;
        } catch (IOException | InterruptedException e) {
            log.debug("Failed to create OVA", e);
        }

        if (!succeeded) {
            log.error("Failed to create OVA. Please check logs for more details: {}", command.logFile());
        }

        return succeeded;
    }

    private String genOvfParameter(String ovf) {
        // replace " characters with \\\" inside the OVF
        return String.format("'%s'", ovf.replaceAll("\"", "\\\\\\\\\\\\\""));
    }

    private String genDiskParameters(Collection<DiskImage> disks, Map<Guid, String> diskIdToPath) {
        return disks.stream()
                .map(disk -> String.format("%s::%s",
                        diskIdToPath.get(disk.getId()),
                        String.valueOf(disk.getActualSizeInBytes())))
                .collect(Collectors.joining("+"));
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }

}
