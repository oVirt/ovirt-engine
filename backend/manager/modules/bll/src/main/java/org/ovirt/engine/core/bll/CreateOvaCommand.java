package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.action.CreateOvaParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandBuilder;
import org.ovirt.engine.core.common.utils.ansible.AnsibleConstants;
import org.ovirt.engine.core.common.utils.ansible.AnsibleExecutor;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnCode;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnValue;
import org.ovirt.engine.core.common.utils.ansible.AnsibleVerbosity;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
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
    private VmHandler vmHandler;
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

    public static final String CREATE_OVA_LOG_DIRECTORY = "ova";
    public static final Pattern DISK_TARGET_SIZE_PATTERN = Pattern.compile("required size: ([0-9]+).*", Pattern.DOTALL);
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
        fillDiskApparentSize(disks, diskIdToPath);
        String ovf = createOvf(disks);
        log.debug("Exporting OVF: {}", ovf);
        boolean succeeded = runAnsiblePackOvaPlaybook(ovf, disks, diskIdToPath);
        teardownImages(disks);
        setSucceeded(succeeded);
    }

    private String createOvf(Collection<DiskImage> disks) {
        switch(getParameters().getEntityType()) {
        case TEMPLATE:
            VmTemplate template = vmTemplateDao.get(getParameters().getEntityId());
            vmHandler.updateVmInitFromDB(template, true);
            vmDeviceUtils.setVmDevices(template);
            List<VmNetworkInterface> interfaces = vmNetworkInterfaceDao.getAllForTemplate(template.getId());
            template.setInterfaces(interfaces);
            FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(template);
            fullEntityOvfData.setDiskImages(new ArrayList<>(disks));
            fullEntityOvfData.setInterfaces(interfaces);
            return ovfManager.exportOva(template, fullEntityOvfData, template.getCompatibilityVersion());

        default:
            VM vm = vmDao.get(getParameters().getEntityId());
            vmHandler.updateVmInitFromDB(vm.getStaticData(), true);
            interfaces = vmNetworkInterfaceDao.getAllForVm(vm.getId());
            vm.setInterfaces(interfaces);
            vmDeviceUtils.setVmDevices(vm.getStaticData());
            fullEntityOvfData = new FullEntityOvfData(vm);
            fullEntityOvfData.setDiskImages(new ArrayList<>(disks));
            fullEntityOvfData.setInterfaces(interfaces);
            return ovfManager.exportOva(vm, fullEntityOvfData, vm.getCompatibilityVersion());
        }
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

    private void fillDiskApparentSize(Collection<DiskImage> disks, Map<Guid, String> diskIdToPath) {
        disks.forEach(destination -> {
            String output = runAnsibleImageMeasurePlaybook(diskIdToPath.get(destination.getId()));
            Matcher matcher = DISK_TARGET_SIZE_PATTERN.matcher(output);
            if (!matcher.find()) {
                log.error("failed to measure image, output: {}", output);
                throw new EngineException(EngineError.GeneralException, "Failed to measure image");
            }
            destination.setActualSizeInBytes(Long.parseLong(matcher.group(1)));
        });
    }

    private String runAnsibleImageMeasurePlaybook(String path) {
        AnsibleCommandBuilder command = new AnsibleCommandBuilder()
                .hostnames(getVds().getHostName())
                .variable("image_path", path)
                // /var/log/ovirt-engine/ova/ovirt-export-ova-ansible-{hostname}-{correlationid}-{timestamp}.log
                .logFileDirectory(CREATE_OVA_LOG_DIRECTORY)
                .logFilePrefix("ovirt-image-measure-ansible")
                .logFileName(getVds().getHostName())
                .logFileSuffix(getCorrelationId())
                .verboseLevel(AnsibleVerbosity.LEVEL0)
                .stdoutCallback(AnsibleConstants.IMAGE_MEASURE_CALLBACK_PLUGIN)
                .playbook(AnsibleConstants.IMAGE_MEASURE_PLAYBOOK);

        AnsibleReturnValue ansibleReturnValue = ansibleExecutor.runCommand(command);
        boolean succeeded = ansibleReturnValue.getAnsibleReturnCode() == AnsibleReturnCode.OK;
        if (!succeeded) {
            log.error(
                "Failed to measure image: {}. Please check logs for more details: {}",
                ansibleReturnValue.getStderr(),
                command.logFile()
            );
            throw new EngineException(EngineError.GeneralException, "Failed to measure image");
        }

        String a = ansibleReturnValue.getStdout();
        return a;
    }

    private boolean runAnsiblePackOvaPlaybook(String ovf, Collection<DiskImage> disks, Map<Guid, String> diskIdToPath) {
        String encodedOvf = genOvfParameter(ovf);
        AnsibleCommandBuilder command = new AnsibleCommandBuilder()
                .hostnames(getVds().getHostName())
                .variable("target_directory", getParameters().getDirectory())
                .variable("entity_type", getParameters().getEntityType().name().toLowerCase())
                .variable("ova_size", String.valueOf(calcOvaSize(disks, encodedOvf)))
                .variable("ova_name", getParameters().getName())
                .variable("ovirt_ova_pack_ovf", encodedOvf)
                .variable("ovirt_ova_pack_disks", genDiskParameters(disks, diskIdToPath))
                // /var/log/ovirt-engine/ova/ovirt-export-ova-ansible-{hostname}-{correlationid}-{timestamp}.log
                .logFileDirectory(CREATE_OVA_LOG_DIRECTORY)
                .logFilePrefix("ovirt-export-ova-ansible")
                .logFileName(getVds().getHostName())
                .logFileSuffix(getCorrelationId())
                .playbook(AnsibleConstants.EXPORT_OVA_PLAYBOOK);

        boolean succeeded = ansibleExecutor.runCommand(command).getAnsibleReturnCode() == AnsibleReturnCode.OK;
        if (!succeeded) {
            log.error("Failed to create OVA. Please check logs for more details: {}", command.logFile());
        }

        return succeeded;
    }

    private long calcOvaSize(Collection<DiskImage> disks, String ovf) {
        // 1 block for the OVF, 1 block per-disk and 2 null-blocks at the end
        return TAR_BLOCK_SIZE * (1 + disks.size() + 2)
                + (int) Math.ceil(ovf.length() / (TAR_BLOCK_SIZE * 1.0)) * TAR_BLOCK_SIZE
                + disks.stream().mapToLong(DiskImage::getActualSizeInBytes).sum();
    }

    private String genOvfParameter(String ovf) {
        // replace " characters with \\\" inside the OVF
        return ovf.replaceAll("\"", "\\\\\\\\\\\\\"");
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
