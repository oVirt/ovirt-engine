package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.ImagesActionsParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@LockIdNameAttribute(fieldName = "VmId")
public class CreateAllSnapshotsFromVmCommand<T extends CreateAllSnapshotsFromVmParameters> extends VmCommand<T> {
    protected CreateAllSnapshotsFromVmCommand(Guid commandId) {
        super(commandId);
    }

    public CreateAllSnapshotsFromVmCommand(T parameters) {
        super(parameters);
        super.setVmId(parameters.getVmId());
        parameters.setEntityId(getVmId());
        setSnapshotName(parameters.getDescription());
    }

    private List<DiskImage> getDisksList() {
        // // if no disk sent or found create snapshot for all vm disks
        // return (selectedDisks.Count > 0) ? selectedDisks : allVmDisks;

        List<DiskImage> allVmDisks = DbFacade.getInstance().getDiskImageDAO().getAllForVm(getVmId());
        List<DiskImage> selectedDisks = LinqUtils.filter(allVmDisks, new Predicate<DiskImage>() {
            @Override
            public boolean eval(DiskImage d) {
                return getParameters().getDisksList().contains(d.getinternal_drive_mapping());
            }
        });

        // if no disk sent or found create snapshot for all vm disks
        return (selectedDisks.size() > 0) ? selectedDisks : allVmDisks;
    }

    @Override
    protected void ExecuteVmCommand() {
        if (getDisksList().size() > 0) {
            if (getParameters().getParentCommand() != VdcActionType.RunVm) {
                lockVmWithCompensationIfNeeded();
            }

            Guid vmSnapshotId = Guid.NewGuid();
            for (DiskImage image : getDisksList()) {
                ImagesActionsParametersBase tempVar = new ImagesActionsParametersBase(image.getId());
                tempVar.setDescription(getParameters().getDescription());
                tempVar.setSessionId(getParameters().getSessionId());
                tempVar.setVmSnapshotId(vmSnapshotId);
                tempVar.setEntityId(getParameters().getEntityId());
                tempVar.setParentCommand(getParameters().getParentCommand() != VdcActionType.Unknown ? getParameters()
                        .getParentCommand() : VdcActionType.CreateAllSnapshotsFromVm);
                ImagesActionsParametersBase p = tempVar;
                // ParametersCurrentUser = CurrentUser,
                getParameters().getImagesParameters().add(p);
                p.setParentParemeters(getParameters());
                VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(
                        VdcActionType.CreateSnapshot, p);

                if (vdcReturnValue.getSucceeded()) {
                    getTaskIdList().addAll(vdcReturnValue.getInternalTaskIdList());
                } else {
                    throw new VdcBLLException(vdcReturnValue.getFault().getError(),
                            "CreateAllSnapshotsFromVmCommand::ExecuteVmCommand: Failed to create snapshot!");
                }
            }
        }

        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_CREATE_SNAPSHOT : AuditLogType.USER_FAILED_CREATE_SNAPSHOT;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_CREATE_SNAPSHOT_FINISHED_SUCCESS
                    : AuditLogType.USER_CREATE_SNAPSHOT_FINISHED_FAILURE;

        default:
            return AuditLogType.USER_CREATE_SNAPSHOT_FINISHED_FAILURE;
        }
    }

    @Override
    protected boolean canDoAction() {
        boolean result = true;
        if (getDisksList().size() > 0) {
            result =
                    ImagesHandler.PerformImagesChecks(getVmId(),
                            getReturnValue().getCanDoActionMessages(),
                            getVm().getstorage_pool_id(),
                            getDisksList().get(0).getstorage_id().getValue(),
                            true,
                            getParameters().getParentCommand() != VdcActionType.RunVm,
                            true,
                            true,
                            true,
                            getParameters().getParentCommand() != VdcActionType.RunVm,
                            true);
        }

        result = result && !IsObjecteLocked();

        if (!result) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CREATE);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__SNAPSHOT);
        }
        return result;
    }

    @Override
    protected VdcActionType getChildActionType() {
        return VdcActionType.CreateSnapshot;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

}
