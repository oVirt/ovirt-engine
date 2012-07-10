package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@LockIdNameAttribute
public class RemoveSnapshotCommand<T extends RemoveSnapshotParameters> extends VmCommand<T> {

    private static final long serialVersionUID = 3162100352844371734L;
    private List<DiskImage> _sourceImages = null;

    public RemoveSnapshotCommand(T parameters) {
        super(parameters);
    }

    private void initializeObjectState() {
        Snapshot snapshot = getSnapshotDao().get(getParameters().getSnapshotId());
        if (snapshot != null) {
            setSnapshotName(snapshot.getDescription());
        }
    }

    private List<DiskImage> getSourceImages() {
        if (_sourceImages == null) {
            _sourceImages = getDiskImageDAO().getAllSnapshotsForVmSnapshot(getParameters().getSnapshotId());
        }
        return _sourceImages;
    }

    @Override
    protected void executeCommand() {
        if (getVm().getstatus() != VMStatus.Down) {
            log.error("Cannot remove VM snapshot. Vm is not Down");
            throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
        }

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                Snapshot snapshot = getSnapshotDao().get(getParameters().getSnapshotId());
                getCompensationContext().snapshotEntityStatus(snapshot, snapshot.getStatus());
                getSnapshotDao().updateStatus(
                        getParameters().getSnapshotId(), SnapshotStatus.LOCKED);
                getCompensationContext().stateChanged();
                return null;
            }
        });
        freeLock();
        getParameters().setEntityId(getVmId());

        for (final DiskImage source : getSourceImages()) {

            // The following line is ok because we have tested in the
            // candoaction that the vm
            // is not a template and the vm is not in preview mode and that
            // this is not the active snapshot.
            DiskImage dest = getDiskImageDAO().getAllSnapshotsForParent(source.getImageId()).get(0);

            ImagesContainterParametersBase tempVar = new ImagesContainterParametersBase(source.getImageId(),
                    getVmId());
            tempVar.setDestinationImageId(dest.getImageId());
            tempVar.setEntityId(getParameters().getEntityId());
            ImagesContainterParametersBase p = tempVar;
            VdcReturnValueBase vdcReturnValue = getBackend().runInternalAction(
                    VdcActionType.RemoveSnapshotSingleDisk,
                    p,
                    ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
            getParameters().getImagesParameters().add(p);

            if (vdcReturnValue != null && vdcReturnValue.getInternalTaskIdList() != null) {
                getReturnValue().getTaskIdList().addAll(vdcReturnValue.getInternalTaskIdList());
            }
        }
        setSucceeded(true);
    }

    @Override
    protected void EndVmCommand() {
        initializeObjectState();
        if (getParameters().getTaskGroupSuccess()) {
            getSnapshotDao().remove(getParameters().getSnapshotId());
        } else {
            getSnapshotDao().updateStatus(getParameters().getSnapshotId(), SnapshotStatus.BROKEN);
        }

        super.EndVmCommand();
    }

    /**
     * @return Don't override the child success, we want merged image chains to be so also in the DB, or else we will be
     *         out of sync with the storage and this is not a good situation.
     */
    @Override
    protected boolean overrideChildCommandSuccess() {
        return false;
    }

    @Override
    protected boolean canDoAction() {
        initializeObjectState();

        if (!validateVmNotDuringSnapshot()) {
            handleCanDoActionFailure();
            return false;
        }

        if (!validateSnapshotExists()) {
            handleCanDoActionFailure();
            return false;
        }

        if (!validateImages()) {
            handleCanDoActionFailure();
            return false;
        }

        // check that we are not deleting the template
        if (!validateImageNotInTemplate()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_REMOVE_IMAGE_TEMPLATE);
            handleCanDoActionFailure();
            return false;
        }

        // check that we are not deleting the vm working snapshot
        if (!validateImageNotActive()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_REMOVE_ACTIVE_IMAGE);
            handleCanDoActionFailure();
            return false;
        }

        getReturnValue().setCanDoAction(true);
        return true;
    }

    private void handleCanDoActionFailure() {
        getReturnValue().setCanDoAction(false);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__SNAPSHOT);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
    }

    protected boolean validateVmNotDuringSnapshot() {
        return validate(createSnapshotValidator().vmNotDuringSnapshot(getVmId()));
    }

    protected boolean validateSnapshotExists() {
        return validate(createSnapshotValidator().snapshotExists(getVmId(), getParameters().getSnapshotId()));
    }

    protected boolean validateImages() {
        return ImagesHandler.PerformImagesChecks(getVm(), getReturnValue().getCanDoActionMessages(),
                getVm().getstorage_pool_id(), Guid.Empty, true, true,
                true, true, true, true, true, true, null);
    }

    protected boolean validateImageNotInTemplate() {
        return getVmTemplateDAO().get(getSourceImages().get(0).getImageId()) == null;
    }

    protected boolean validateImageNotActive() {
        return getDiskImageDAO().get(getSourceImages().get(0).getImageId()) == null;
    }

    protected SnapshotsValidator createSnapshotValidator() {
        return new SnapshotsValidator();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_REMOVE_SNAPSHOT : AuditLogType.USER_FAILED_REMOVE_SNAPSHOT;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_REMOVE_SNAPSHOT_FINISHED_SUCCESS
                    : AuditLogType.USER_REMOVE_SNAPSHOT_FINISHED_FAILURE;

        default:
            return AuditLogType.USER_REMOVE_SNAPSHOT_FINISHED_FAILURE;
        }
    }

    @Override
    protected VdcActionType getChildActionType() {
        return VdcActionType.RemoveSnapshotSingleDisk;
    }

    @Override
    protected Map<String, String> getExclusiveLocks() {
        return Collections.singletonMap(getVmId().toString(), LockingGroup.VM.name());
    }

    protected SnapshotDao getSnapshotDao() {
        return getDbFacade().getSnapshotDao();
    }
}
