package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class RestoreAllSnapshotsCommand<T extends RestoreAllSnapshotsParameters> extends VmCommand<T> {

    private static final long serialVersionUID = -461387501474222174L;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected RestoreAllSnapshotsCommand(Guid commandId) {
        super(commandId);
    }

    public RestoreAllSnapshotsCommand(T parameters) {
        super(parameters);
        parameters.setEntityId(getVmId());
    }

    @Override
    protected void ExecuteVmCommand() {
        if (getImagesList().size() > 0) {
            lockVmWithCompensationIfNeeded();

            VdcReturnValueBase returnValue = null;
            for (DiskImage image : getImagesList()) {
                ImagesContainterParametersBase params = new ImagesContainterParametersBase(image.getId(),
                        image.getinternal_drive_mapping(), getVmId());
                returnValue = runAsyncTask(VdcActionType.RestoreFromSnapshot, params);
            }

            // We should have at least one task in the VDSM, to be sure that EndCommand will be called and the VM would
            // change its status from Image lock.
            if (getTaskIdList().size() == 0) {
                log.errorFormat("Can't restore snapshot for VM, since no destroyImage task could be established in the VDSM.");
                if (returnValue != null) {
                    getReturnValue().setFault(returnValue.getFault());
                }
            } else {
                setSucceeded(true);
            }
        } else {
            setSucceeded(true);
        }
    }

    /**
     * Run the given command as async task, which includes these steps:
     * <ul>
     * <li>Add parent info to task parameters.</li>
     * <li>Run with current command's {@link org.ovirt.engine.core.bll.job.ExecutionContext}.</li>
     * <li>Add son parameters to saved image parameters.</li>
     * <li>Add son task IDs to list of task IDs.</li>
     * </ul>
     *
     * @param taskType
     *            The type of the command to run as async task.
     * @param params
     *            The command parameters.
     * @return The return value from the task.
     */
    private VdcReturnValueBase runAsyncTask(VdcActionType taskType, ImagesContainterParametersBase params) {
        VdcReturnValueBase returnValue;
        params.setEntityId(getParameters().getEntityId());
        params.setParentCommand(getActionType());
        params.setParentParemeters(getParameters());
        returnValue = Backend.getInstance().runInternalAction(
                taskType,
                params,
                ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
        getParameters().getImagesParameters().add(params);
        getTaskIdList().addAll(returnValue.getInternalTaskIdList());
        return returnValue;
    }

    @Override
    protected VdcActionType getChildActionType() {
        return VdcActionType.RestoreFromSnapshot;
    }

    private List<DiskImage> getImagesList() {
        if (getParameters().getImagesList() == null && !getParameters().getDstSnapshotId().equals(Guid.Empty)) {
            getParameters().setImagesList(
                    DbFacade.getInstance()
                            .getDiskImageDAO()
                            .getAllSnapshotsForVmSnapshot(getParameters().getDstSnapshotId()));
        }
        return getParameters().getImagesList();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_RESTORE_FROM_SNAPSHOT_START
                    : AuditLogType.USER_FAILED_RESTORE_FROM_SNAPSHOT;
        default:
            return AuditLogType.USER_RESTORE_FROM_SNAPSHOT_FINISH_SUCCESS;
        }
    }

    @Override
    protected boolean canDoAction() {
        boolean result = false;
        if (getImagesList() == null || getImagesList().isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST);
        } else {
            result = ImagesHandler.PerformImagesChecks(getVmId(), getReturnValue().getCanDoActionMessages(), getVm()
                    .getstorage_pool_id(), getImagesList().get(0).getstorage_ids().get(0), true, true, false, !isInternalExecution(),
                    false, false, true);
            if (result && (getVm().getstatus() != VMStatus.Down)) {
                result = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
            }
        }
        if (!result) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REVERT_TO);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__SNAPSHOT);
        }
        return result;
    }

    @Override
    protected void EndSuccessfully() {
        super.EndSuccessfully();
        if (getImagesList() != null) {
            for (DiskImage image : getImagesList()) {
                DiskImage imageFromDb = DbFacade.getInstance().getDiskImageDAO().getSnapshotById(image.getId());
                if (imageFromDb != null) {
                    setVm(null);
                    getVm().setapp_list(imageFromDb.getappList());
                    DbFacade.getInstance().getVmDynamicDAO().update(getVm().getDynamicData());
                    break;
                }
            }
        }
    }

    private static Log log = LogFactory.getLog(RemoveAllVmImagesCommand.class);
}
