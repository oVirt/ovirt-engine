package org.ovirt.engine.core.bll.exportimport;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.CreateOvaCommand;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateAllOvaDisksParameters;
import org.ovirt.engine.core.common.action.CreateOvaParameters;
import org.ovirt.engine.core.common.action.ExportOvaParameters;
import org.ovirt.engine.core.common.action.ExportOvaParameters.Phase;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandBuilder;
import org.ovirt.engine.core.common.utils.ansible.AnsibleConstants;
import org.ovirt.engine.core.common.utils.ansible.AnsibleExecutor;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnCode;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

@NonTransactiveCommandAttribute
public class ExportOvaCommand<T extends ExportOvaParameters> extends CommandBase<T> implements SerialChildExecutingCommand {

    @Inject
    private DiskDao diskDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private DiskVmElementDao diskVmElementDao;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;
    @Inject
    private AnsibleExecutor ansibleExecutor;

    public ExportOvaCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        setVmId(getParameters().getEntityId());
        if (getVm() == null) {
            return;
        }
        String path = getParameters().getDirectory();
        if (path != null && path.endsWith("/")) {
            getParameters().setDirectory(path.substring(0, path.length()-1));
        }
        if (StringUtils.isEmpty(getParameters().getName())) {
            getParameters().setName(String.format("%s.ova", getVm().getName()));
        }
        setStoragePoolId(getVm().getStoragePoolId());
        setVdsId(getParameters().getProxyHostId());
        if (getParameters().getDiskInfoDestinationMap() == null) {
            // TODO: map to different storage domains
            List<DiskImage> disks = getDisks();
            Map<DiskImage, DiskImage> disksMapping = disks.stream()
                    .collect(Collectors.toMap(d -> d, this::map));
            getParameters().setDiskInfoDestinationMap(disksMapping);
        }
    }

    @Override
    protected boolean validate() {
        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (getParameters().getProxyHostId() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_PROXY_HOST_MUST_BE_SPECIFIED);
        }

        HostValidator hostValidator = HostValidator.createInstance(getVds());
        if (!validate(hostValidator.hostExists())) {
            return false;
        }

        if (!validate(hostValidator.isUp())) {
            return false;
        }

        StoragePoolValidator spValidator = new StoragePoolValidator(getStoragePool());
        if (!validate(spValidator.exists())) {
            return false;
        }

        if (!validate(spValidator.isInStatus(StoragePoolStatus.Up))) {
            return false;
        }

        if (!getStoragePoolId().equals(getVds().getStoragePoolId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_PROXY_HOST_NOT_IN_VM_DATA_CENTER);
        }

        if (!validate(validateTargetFolder())) {
            return false;
        }

        return true;
    }

    private ValidationResult validateTargetFolder() {
        AnsibleCommandBuilder command = new AnsibleCommandBuilder()
                .hostnames(getVds().getHostName())
                .variables(
                    new Pair<>("target_directory", getParameters().getDirectory()),
                    new Pair<>("validate_only", "True")
                )
                // /var/log/ovirt-engine/ova/ovirt-export-ova-validate-ansible-{hostname}-{correlationid}-{timestamp}.log
                .logFileDirectory(CreateOvaCommand.CREATE_OVA_LOG_DIRECTORY)
                .logFilePrefix("ovirt-export-ova-validate-ansible")
                .logFileName(getVds().getHostName())
                .logFileSuffix(getCorrelationId())
                .playbook(AnsibleConstants.EXPORT_OVA_PLAYBOOK);

        boolean succeeded = false;
        try {
            succeeded = ansibleExecutor.runCommand(command).getAnsibleReturnCode() == AnsibleReturnCode.OK;
        } catch (IOException | InterruptedException e) {
            log.error("Invalid target for OVA (directory={}, host={}): {}",
                    getParameters().getDirectory(),
                    getVdsName(),
                    e.getMessage());
            log.debug("Exception", e);
        }

        return succeeded ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_INVALID_OVA_DESTINATION_FOLDER,
                        String.format("$vdsName %s", getVdsName()),
                        String.format("$directory %s", getParameters().getDirectory()));
    }

    @Override
    protected void executeCommand() {
        createTemporaryDisks();
        setSucceeded(true);
    }

    private Map<DiskImage, DiskImage> createTemporaryDisks() {
        ActionReturnValue returnValue = runInternalAction(
                ActionType.CreateAllOvaDisks,
                buildCreateAllOvaDisksParameters(),
                ExecutionHandler.createDefaultContextForTasks(getContext()));

        if (!returnValue.getSucceeded()) {
            log.error("Failed to create OVA disks");
            throw new EngineException(returnValue.getFault().getError(), returnValue.getFault().getMessage());
        }
        return returnValue.getActionReturnValue();
    }

    private CreateAllOvaDisksParameters buildCreateAllOvaDisksParameters() {
        CreateAllOvaDisksParameters parameters = new CreateAllOvaDisksParameters();
        parameters.setDiskInfoDestinationMap(getParameters().getDiskInfoDestinationMap());
        parameters.setEntityId(getParameters().getEntityId());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        return parameters;
    }

    private DiskImage map(DiskImage image) {
        DiskImage destination = DiskImage.copyOf(image);
        destination.setParentId(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        destination.setImageTemplateId(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        destination.setVmSnapshotId(null);
        destination.setActive(true);
        destination.setVolumeFormat(VolumeFormat.COW);
        destination.setVolumeType(VolumeType.Sparse);
        destination.setCreationDate(new Date());
        destination.setId(Guid.newGuid());
        destination.setImageId(Guid.newGuid());
        destination.setDiskAlias(image.getDiskAlias());
        destination.setDiskDescription(image.getDiskDescription());
        return destination;
    }

    private List<DiskImage> getDisks() {
        if (getParameters().getEntityType() == VmEntityType.TEMPLATE) {
            // TODO: add the ability to export a template
            return Collections.emptyList();
        }
        else {
            List<Disk> allDisks = diskDao.getAllForVm(getParameters().getEntityId());
            return DisksFilter.filterImageDisks(allDisks, ONLY_NOT_SHAREABLE, ONLY_ACTIVE);
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionSubjects = new ArrayList<>();
        permissionSubjects.add(new PermissionSubject(
                getParameters().getEntityId(),
                getParameters().getEntityType() == VmEntityType.VM ? VdcObjectType.VM : VdcObjectType.VmTemplate,
                        getActionType().getActionGroup()));
        return permissionSubjects;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        switch(getParameters().getPhase()) {
        case CREATE_DISKS:
            getParameters().setPhase(Phase.CREATE_OVA);
            break;

        case CREATE_OVA:
            getParameters().setPhase(Phase.REMOVE_DISKS);
            break;

        case REMOVE_DISKS:
            return false;

        default:
        }

        persistCommandIfNeeded();
        executeNextOperation();
        return true;
    }

    private void executeNextOperation() {
        switch (getParameters().getPhase()) {
            case CREATE_OVA:
                createOva();
                break;

            case REMOVE_DISKS:
                removeTemporaryDisks();
                break;
        }
    }

    private void createOva() {
        ActionReturnValue returnValue = runInternalAction(ActionType.CreateOva,
                buildCreateOvaParameters(),
                ExecutionHandler.createDefaultContextForTasks(getContext()));

        if (!returnValue.getSucceeded()) {
            log.error("Failed to create OVA file");
            throw new EngineException(returnValue.getFault().getError(), returnValue.getFault().getMessage());
        }
    }

    private void removeTemporaryDisks() {
        ThreadPoolUtil.execute(() -> getParameters().getDiskInfoDestinationMap().values()
                .stream()
                .map(disk -> new RemoveDiskParameters(disk.getId()))
                .forEach(params -> {
                    params.setForceDelete(true);
                    runInternalAction(
                            ActionType.RemoveDisk,
                            params);
                }));
    }

    private CreateOvaParameters buildCreateOvaParameters() {
        CreateOvaParameters parameters = new CreateOvaParameters();
        parameters.setVm(vmDao.get(getParameters().getEntityId()));
        getParameters().getDiskInfoDestinationMap().forEach((source, destination) -> {
            // same as the disk<->vm element for the original disk
            destination.setDiskVmElements(Collections.singleton(diskVmElementDao.get(new VmDeviceId(source.getId(), getParameters().getEntityId()))));
        });
        parameters.setDiskInfoDestinationMap(getParameters().getDiskInfoDestinationMap());
        parameters.setProxyHostId(getParameters().getProxyHostId());
        parameters.setDirectory(getParameters().getDirectory());
        parameters.setName(getParameters().getName());
        return parameters;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getEntityId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    };

    @Override
    protected void endWithFailure() {
        removeTemporaryDisks();
        super.endWithFailure();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ?
                    AuditLogType.IMPORTEXPORT_STARTING_EXPORT_VM_TO_OVA
                    : AuditLogType.IMPORTEXPORT_EXPORT_VM_TO_OVA_FAILED;

        case END_SUCCESS:
            return getSucceeded() ?
                    AuditLogType.IMPORTEXPORT_EXPORT_VM_TO_OVA
                    : AuditLogType.IMPORTEXPORT_EXPORT_VM_TO_OVA_FAILED;

        default:
            return AuditLogType.IMPORTEXPORT_EXPORT_VM_TO_OVA_FAILED;
        }
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        Map<String, String> jobProperties = super.getJobMessageProperties();
        jobProperties.put("ovapath", getOvaPath());
        jobProperties.put(VdcObjectType.VDS.name().toLowerCase(), getVdsName());
        return jobProperties;
    }

    /**
     * Used for the execution job
     */
    public String getOvaPath() {
        return String.format("%s/%s", getParameters().getDirectory(), getParameters().getName());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__EXPORT);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }
}
