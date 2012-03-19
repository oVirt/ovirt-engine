package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.TryBackToAllSnapshotsOfVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;

public class TryBackToAllSnapshotsOfVmCommand<T extends TryBackToAllSnapshotsOfVmParameters> extends VmCommand<T> {

    private static final long serialVersionUID = 2636628918352438919L;

    private SnapshotsManager snapshotsManager = new SnapshotsManager();

    public TryBackToAllSnapshotsOfVmCommand(T parameters) {
        super(parameters);
        parameters.setEntityId(getVmId());
    }

    @Override
    protected void EndWithFailure() {
        Guid previouslyActiveSnapshotId =
                getSnapshotDao().getId(getVmId(), SnapshotType.PREVIEW, SnapshotStatus.LOCKED);
        getSnapshotDao().remove(previouslyActiveSnapshotId);
        getSnapshotDao().remove(getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE));

        snapshotsManager.addActiveSnapshot(previouslyActiveSnapshotId, getVm(), getCompensationContext());

        super.EndWithFailure();
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    @Override
    protected void EndSuccessfully() {
        EndActionOnDisks();

        if (getVm() != null) {
            VmHandler.unlockVm(getVm().getDynamicData(), getCompensationContext());

            getSnapshotDao().updateStatus(getParameters().getDstSnapshotId(), SnapshotStatus.IN_PREVIEW);
            getSnapshotDao().updateStatus(getSnapshotDao().getId(getVm().getId(),
                    SnapshotType.PREVIEW,
                    SnapshotStatus.LOCKED),
                    SnapshotStatus.OK);

            snapshotsManager.attempToRestoreVmConfigurationFromSnapshot(getVm(),
                    getSnapshotDao().get(getParameters().getDstSnapshotId()),
                    getSnapshotDao().getId(getVm().getId(), SnapshotType.ACTIVE),
                    getCompensationContext());
            UpdateVmInSpm(getVm().getstorage_pool_id(),
                    new java.util.ArrayList<VM>(java.util.Arrays.asList(new VM[] { getVm() })));
        } else {
            setCommandShouldBeLogged(false);
            log.warn("VmCommand::EndVmCommand: Vm is null - not performing EndAction on Vm");
        }

        setSucceeded(true);
    }

    @Override
    protected void ExecuteVmCommand() {
        Guid previousActiveSnapshotId = getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE);
        getSnapshotDao().remove(previousActiveSnapshotId);
        snapshotsManager.addSnapshot(previousActiveSnapshotId,
                "Active VM before the preview",
                        SnapshotType.PREVIEW,
                        getVm(),
                        getCompensationContext());
        Guid newActiveSnapshotId = Guid.NewGuid();
        snapshotsManager.addActiveSnapshot(newActiveSnapshotId, getVm(), getCompensationContext());
        snapshotsManager.removeAllIllegalDisks(previousActiveSnapshotId);

        List<DiskImage> images = DbFacade
                .getInstance()
                .getDiskImageDAO()
                .getAllSnapshotsForVmSnapshot(getParameters().getDstSnapshotId());
        if (images.size() > 0) {
            VmHandler.checkStatusAndLockVm(getVmId(), getCompensationContext());
            for (DiskImage image : images) {
                ImagesContainterParametersBase tempVar = new ImagesContainterParametersBase(image.getId(),
                        image.getinternal_drive_mapping(), getVmId());
                tempVar.setParentCommand(VdcActionType.TryBackToAllSnapshotsOfVm);
                tempVar.setVmSnapshotId(newActiveSnapshotId);
                tempVar.setEntityId(getParameters().getEntityId());
                tempVar.setParentParemeters(getParameters());
                ImagesContainterParametersBase p = tempVar;
                VdcReturnValueBase vdcReturnValue =
                        Backend.getInstance().runInternalAction(VdcActionType.TryBackToSnapshot,
                                p,
                                ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
                getParameters().getImagesParameters().add(p);

                if (vdcReturnValue.getSucceeded()) {
                    getTaskIdList().addAll(vdcReturnValue.getInternalTaskIdList());
                } else if (vdcReturnValue.getFault() != null) {
                    // if we have a fault, forward it to the user
                    throw new VdcBLLException(vdcReturnValue.getFault().getError(), vdcReturnValue.getFault()
                            .getMessage());
                } else {
                    log.error("Cannot create snapshot");
                    throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
                }
            }
        } else {
            getSnapshotDao().updateStatus(previousActiveSnapshotId, SnapshotStatus.OK);
        }

        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_TRY_BACK_TO_SNAPSHOT
                    : AuditLogType.USER_FAILED_TRY_BACK_TO_SNAPSHOT;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_TRY_BACK_TO_SNAPSHOT_FINISH_SUCCESS
                    : AuditLogType.USER_TRY_BACK_TO_SNAPSHOT_FINISH_FAILURE;

        default:
            return AuditLogType.USER_TRY_BACK_TO_SNAPSHOT_FINISH_FAILURE;
        }
    }

    @Override
    protected boolean canDoAction() {
        VmHandler.updateDisksFromDb(getVm());
        DiskImage vmDisk = LinqUtils.first(getVm().getDiskMap().values());
        boolean result = true;

        result = result && validate(new SnapshotsValidator().vmNotDuringSnapshot(getVmId()));

        if (vmDisk != null) {
            result =
                    result
                            && ImagesHandler.PerformImagesChecks(getVm(),
                                    getReturnValue().getCanDoActionMessages(),
                                    getVm().getstorage_pool_id(),
                                    Guid.Empty,
                                    true,
                                    true,
                                    false,
                                    false,
                                    true,
                                    true,
                                    true, true, new ArrayList<DiskImage>(getVm().getDiskMap().values()));
        }
        if (result && LinqUtils.foreach(getVm().getDiskMap().values(), new Function<DiskImage, Guid>() {
            @Override
            public Guid eval(DiskImage disk) {
                return disk.getvm_snapshot_id().getValue();
            }
        }).contains(getParameters().getDstSnapshotId())) {
            result = false;
            addCanDoActionMessage(VdcBllMessages.CANNOT_PREIEW_CURRENT_IMAGE);
        }

        if (!result) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__PREVIEW);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__SNAPSHOT);
        }
        return result;
    }

    @Override
    protected VdcActionType getChildActionType() {
        return VdcActionType.TryBackToSnapshot;
    }
}
