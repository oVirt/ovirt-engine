package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveDisksFromVmParameters;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class RemoveDisksFromVmCommand<T extends RemoveDisksFromVmParameters> extends VmCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected RemoveDisksFromVmCommand(Guid commandId) {
        super(commandId);
    }

    public RemoveDisksFromVmCommand(T parameters) {
        super(parameters);
        setVmId(parameters.getVmId());
        parameters.setEntityId(getVmId());
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = true;
        if (getVm() == null) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }
        for (Guid imageId : getParameters().getImageIds()) {
            DiskImage disk = DbFacade.getInstance().getDiskImageDAO().get(imageId);
            if (disk == null) {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
                break;
            }
            retValue = retValue
                    && ImagesHandler.PerformImagesChecks(getVmId(), getReturnValue().getCanDoActionMessages(), getVm()
                            .getstorage_pool_id(), disk.getstorage_id().getValue(), false, true, false, false, true,
                            true, true);
            if (!retValue) {
                break;
            }
        }

        if (!retValue) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
        }
        return retValue;
    }

    @Override
    protected void ExecuteVmCommand() {
        VmHandler.LockVm(getVm().getDynamicData(), getCompensationContext());

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                for (Guid imageId : getParameters().getImageIds()) {
                    RemoveImageParameters tempVar = new RemoveImageParameters(imageId, null, getVmId());
                    tempVar.setParentCommand(VdcActionType.RemoveDisksFromVm);
                    tempVar.setEntityId(getParameters().getEntityId());
                    RemoveImageParameters p = tempVar;
                    p.setParentParemeters(getParameters());
                    VdcReturnValueBase vdcReturnValue =
                            Backend.getInstance().runInternalAction(VdcActionType.RemoveImage, p);
                    getParameters().getImagesParameters().add(p);
                    getReturnValue().getTaskIdList().addAll(vdcReturnValue.getInternalTaskIdList());
                    if (!vdcReturnValue.getSucceeded()) {
                        setSucceeded(false);
                        break;
                    }
                    setSucceeded(vdcReturnValue.getSucceeded());
                }
                return null;
            }
        });
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_DISK_FROM_VM : AuditLogType.USER_FAILED_REMOVE_DISK_FROM_VM;
    }

    @Override
    protected void EndVmCommand() {
        setCommandShouldBeLogged(false);
        super.EndVmCommand();
    }

    @Override
    protected VdcActionType getChildActionType() {
        return VdcActionType.RemoveImage;
    }
}
