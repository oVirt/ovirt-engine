package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.TryBackToAllSnapshotsOfVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;

public class TryBackToAllSnapshotsOfVmCommand<T extends TryBackToAllSnapshotsOfVmParameters> extends VmCommand<T> {

    private static final long serialVersionUID = 2636628918352438919L;

    public TryBackToAllSnapshotsOfVmCommand(T parameters) {
        super(parameters);
        parameters.setEntityId(getVmId());

    }

    @Override
    protected void EndSuccessfully() {
        super.EndSuccessfully();
        List<DiskImage> images = DbFacade
                .getInstance()
                .getDiskImageDAO()
                .getAllSnapshotsForVmSnapshot(getParameters().getDstSnapshotId());
        if (images.size() != 0) {
            setVm(null);
            getVm().setapp_list(images.get(0).getappList());
            DbFacade.getInstance().getVmDynamicDAO().update(getVm().getDynamicData());
        }
    }

    @Override
    protected void ExecuteVmCommand() {
        List<DiskImage> images = DbFacade
                .getInstance()
                .getDiskImageDAO()
                .getAllSnapshotsForVmSnapshot(getParameters().getDstSnapshotId());
        Guid vmSnapshotId = Guid.NewGuid();
        VmHandler.checkStatusAndLockVm(getVmId(), getCompensationContext());

        if (images.size() > 0) {
            for (DiskImage image : images) {
                ImagesContainterParametersBase tempVar = new ImagesContainterParametersBase(image.getId(),
                        image.getinternal_drive_mapping(), getVmId());
                tempVar.setParentCommand(VdcActionType.TryBackToAllSnapshotsOfVm);
                tempVar.setVmSnapshotId(vmSnapshotId);
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
        // DiskImage vmDisk = null; // LINQ 32934 Vm.DiskMap.Select(a =>
        // a.Value).FirstOrDefault();
        DiskImage vmDisk = LinqUtils.first(getVm().getDiskMap().values());
        boolean result = true;
        if (vmDisk != null) {
            result = ImagesHandler.PerformImagesChecks(getVmId(), getReturnValue().getCanDoActionMessages(), getVm()
                    .getstorage_pool_id(), vmDisk.getstorage_id().getValue(), true, true, false, false, true, true,
                    true);
        }
        // check that not trying to preview current images (leaf)
        // LINQ 29456
        // if (result && Vm.DiskMap.Values.Select(disk =>
        // disk.vm_snapshot_id.Value).
        // Contains(TryParameters.DstSnapshotId))
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
