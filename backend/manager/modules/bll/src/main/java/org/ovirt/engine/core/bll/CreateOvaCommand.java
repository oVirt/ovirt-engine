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
import org.ovirt.engine.core.common.action.CreateOvaParameters;
import org.ovirt.engine.core.common.action.VmExternalDataKind;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.ovf.OvfManager;
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

    private Pair<String, Version> createOvf(Collection<DiskImage> disks) {
        switch(getParameters().getEntityType()) {
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
            String tpmData,
            String nvramData,
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
        vars.put("ovirt_ova_pack_tpm", tpmData);
        vars.put("ovirt_ova_pack_nvram", nvramData);
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

    private String getTpmData() {
        var tpmDataAndHash = vmDao.getTpmData(getParameters().getEntityId());
        return tpmDataAndHash != null ? tpmDataAndHash.getFirst() : null;
    }

    private String getNvramData() {
        var nvramDataAndHash = vmDao.getNvramData(getParameters().getEntityId());
        return nvramDataAndHash != null ? nvramDataAndHash.getFirst() : null;
    }

    private Map<VmExternalDataKind, String> getVmExternalData() {
        Map<VmExternalDataKind, String> externalData = new HashMap<>();
        String tpmData = getTpmData();
        if (!StringUtils.isEmpty(tpmData)) {
            externalData.put(VmExternalDataKind.TPM, tpmData);
        }
        String nvramData = getNvramData();
        if (!StringUtils.isEmpty(nvramData)) {
            externalData.put(VmExternalDataKind.NVRAM, nvramData);
        }
        return externalData;
    }

    private void updateDiskVmElementFromDb(DiskImage diskImage) {
        diskHandler.updateDiskVmElementFromDb(diskImage, getParameters().getEntityId());
    }

    private long calcOvaSize(Collection<DiskImage> disks, String tpmData, String nvramData, String ovf) {
        // 1 block for the OVF, 1 block per-disk and 2 null-blocks at the end
        return TAR_BLOCK_SIZE * (1 + disks.size() + 2)
                + blockAlignedSize(ovf.length())
                + (tpmData != null ? blockAlignedSize(tpmData.length()) : 0)
                + (nvramData != null ? blockAlignedSize(nvramData.length()) : 0)
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
        var diskPathToSize = disks.stream()
                .collect(Collectors.toMap(
                        disk -> diskIdToPath.get(disk.getId()),
                        disk -> disk.getActualSizeInBytes()));
        String json;
        try {
            json = new ObjectMapper().writeValueAsString(diskPathToSize);
        } catch (IOException e) {
            throw new RuntimeException("failed to serialize disk info");
        }
        return encode(json);
    }

    private void teardown() {
        if (getParameters().getEntityType() == VmEntityType.TEMPLATE
                || vmDao.get(getParameters().getEntityId()).isDown()) {
            teardownImages(getParameters().getDisks());
        }
        setSucceeded(true);
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        switch(getParameters().getPhase()) {
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
