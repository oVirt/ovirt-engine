package org.ovirt.engine.core.bll.storage.disk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.LockMessage;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.MoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;

@NonTransactiveCommandAttribute
public class MoveDiskCommand<T extends MoveDiskParameters> extends CommandBase<T> {

    @Inject
    private VmDao vmDao;
    @Inject
    private DiskVmElementDao diskVmElementDao;
    @Inject
    private DiskImageDao diskImageDao;

    private String cachedDiskIsBeingMigratedMessage;


    public MoveDiskCommand(Guid commandId) {
        super(commandId);
    }

    public MoveDiskCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        DiskImage diskImage = diskImageDao.getAncestor(getParameters().getImageId());

        if (diskImage == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
        }
        if (diskImage.getImageStatus() == ImageStatus.LOCKED) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_IS_BEING_MIGRATED,
                    ReplacementUtils.createSetVariableString("DiskName", diskImage.getDiskAlias()));
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        List<DiskVmElement> diskVmElements = diskVmElementDao.getAllDiskVmElementsByDiskId(getParameters().getImageGroupID());
        ActionType actionType = getMoveActionType(diskVmElements);

        if (actionType == ActionType.MoveOrCopyDisk) {
            MoveDiskParameters moveDiskParameters = getParameters();

            // This is required to make MoveDiskCommand and MoveOrCopyDiskCommand have
            // different a commandId. Having the same commandId will make CommandsCache
            // return the wrong command and lead to unexpected results
            moveDiskParameters.setCommandId(null);

            setReturnValue(runInternalAction(actionType,
                    moveDiskParameters,
                    ExecutionHandler.createInternalJobContext(getContext(), getLock())));
        } else {
            Guid vmId = diskVmElements.get(0).getVmId();
            EngineLock engineLock = lockVmWithWait(vmId);
            setReturnValue(runInternalAction(actionType,
                    createLiveMigrateDiskParameters(getParameters(), vmId),
                    ExecutionHandler.createInternalJobContext(getContext(), engineLock)));
        }
        setSucceeded(true);
    }

    protected ActionType getMoveActionType(List<DiskVmElement> diskVmElements) {
        // Floating disk
        if (diskVmElements.isEmpty()) {
            return ActionType.MoveOrCopyDisk;
        }

        // In case of a shareable disk, the validation
        // will be performed in MoveOrCopyDiskCommand
        // which both action types use.
        DiskVmElement diskVmElement = diskVmElements.get(0);
        VM vm = vmDao.get(diskVmElement.getVmId());
        // If the VM is null the operation is for a template disk
        if (vm == null || vm.isDown() || !diskVmElement.isPlugged()) {
            return ActionType.MoveOrCopyDisk;
        }

        return ActionType.LiveMigrateDisk;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__MOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getParameters().getImageGroupID(),
                VdcObjectType.Disk,
                ActionGroup.CONFIGURE_DISK_STORAGE));
        permissionList.add(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage, ActionGroup.CREATE_DISK));

        return permissionList;
    }

    private LiveMigrateDiskParameters createLiveMigrateDiskParameters(MoveDiskParameters moveDiskParameters, Guid vmId) {
        LiveMigrateDiskParameters params = new LiveMigrateDiskParameters(moveDiskParameters.getImageId(),
                moveDiskParameters.getSourceDomainId(),
                moveDiskParameters.getStorageDomainId(),
                vmId,
                moveDiskParameters.getQuotaId(),
                moveDiskParameters.getDiskProfileId(),
                getParameters().getImageGroupID());
        // Pass down correlation ID, useful when it's set externally (e.g. via the API)
        params.setCorrelationId(getCorrelationId());
        return params;
    }

    protected EngineLock lockVmWithWait(Guid vmId) {
        EngineLock liveStorageMigrationEngineLock = new EngineLock();
        liveStorageMigrationEngineLock.setExclusiveLocks(Collections.singletonMap(vmId.toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.LIVE_STORAGE_MIGRATION,
                        EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED)));
        lockManager.acquireLockWait(liveStorageMigrationEngineLock);
        return liveStorageMigrationEngineLock;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(
                getParameters().getImageGroupID().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK,
                        getDiskIsBeingMigratedMessage()));
    }

    private String getDiskIsBeingMigratedMessage() {
        if (cachedDiskIsBeingMigratedMessage == null) {
            cachedDiskIsBeingMigratedMessage = new LockMessage(EngineMessage.ACTION_TYPE_FAILED_DISK_IS_BEING_MIGRATED)
                    .withOptional("DiskName", getParameters().getNewAlias())
                    .toString();
        }

        return cachedDiskIsBeingMigratedMessage;
    }
}
