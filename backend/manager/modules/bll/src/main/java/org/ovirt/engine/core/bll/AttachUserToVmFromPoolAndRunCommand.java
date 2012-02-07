package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmPoolUserParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@LockIdNameAttribute(fieldName = "AdUserId")
public class AttachUserToVmFromPoolAndRunCommand<T extends VmPoolUserParameters> extends
VmPoolUserCommandBase<T> {
    protected AttachUserToVmFromPoolAndRunCommand(Guid commandId) {
        super(commandId);
    }

    public AttachUserToVmFromPoolAndRunCommand(T parameters) {
        super(parameters);
    }

    /**
     * This lock is used to synchronize multiple users trying to attach a VM from pool, so that they won't be able to
     * attach the same VM to more than one user.
     */
    private static final Object _lockObject = new Object();

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;

        synchronized (_lockObject) {
            // no available VMs:
            if (GetVmToAttach(getParameters().getVmPoolId()).equals(Guid.Empty))
            {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NO_AVAILABLE_POOL_VMS);
                returnValue = false;
            }
        }

        // check user isn't already attached to vm from this pool
        if (returnValue) {
            List<VM> vmsForUser = DbFacade.getInstance().getVmDAO().getAllForUser(getAdUserId());

            for (VM vm : vmsForUser) {
                if (vm.getVmPoolId() != null && getVmPoolId().equals(vm.getVmPoolId())) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_USER_ATTACHED_TO_POOL);
                    returnValue = false;
                }
            }
        }
        if (!returnValue) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ALLOCATE_AND_RUN);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_FROM_VM_POOL);
        }
        return returnValue;
    }

    @Override
    public Guid getVmId() {
        return getParameters().getVmId();
    }

    @Override
    public void setVmId(Guid value) {
        getParameters().setVmId(value);
    }

    @Override
    protected NGuid getVmPoolId() {
        return getParameters().getVmPoolId();
    }

    @Override
    protected void setVmPoolId(NGuid value) {
        getParameters().setVmPoolId(value);
    }

    @Override
    public Guid getAdUserId() {
        return getParameters().getUserId();
    }

    @Override
    protected TransactionScopeOption getTransactionScopeOption() {
        return getActionState() != CommandActionState.EXECUTE ? TransactionScopeOption.Suppress : super
                .getTransactionScopeOption();
    }

    @Override
    protected void executeCommand() {
        getParameters().setParentCommand(VdcActionType.AttachUserToVmFromPoolAndRun);

        // we are setting 'Vm' since VmId is overriden and 'Vm' is null
        // (since 'Vm' is dependant on 'mVmId', which is not set here).
        setVm(DbFacade.getInstance().getVmDAO().getById(getVmId()));

        /**
         * TODO: check users throw their groups as well
         */
        initUser();
        synchronized (_lockObject) {
            // check vm is not attached to user and attach
            List<permissions> vmUserPermissions = DbFacade
                    .getInstance()
                    .getPermissionDAO()
                    .getAllForRoleAndObject(PredefinedRoles.ENGINE_USER.getId(),
                            getVmId());
            if (vmUserPermissions == null || vmUserPermissions.isEmpty()) {
                setVmId(GetVmToAttach(getParameters().getVmPoolId()));
                if (!getVmId().equals(Guid.Empty)) {
                    getParameters().setEntityId(getVmId());
                    permissions perm = new permissions(getAdUserId(), PredefinedRoles.ENGINE_USER.getId(), getVmId(),
                            VdcObjectType.VM);
                    PermissionsOperationsParametes permParams = new PermissionsOperationsParametes(perm);
                    permParams.setShouldBeLogged(false);
                    permParams.setParentCommand(VdcActionType.AttachUserToVmFromPoolAndRun);
                    VdcReturnValueBase vdcReturnValueFromAddPerm = Backend.getInstance().runInternalAction(VdcActionType.AddPermission,
                            permParams,
                            (CommandContext) getCompensationContext());
                    if (!vdcReturnValueFromAddPerm.getSucceeded()) {
                        log.infoFormat("Failed to give user {0} permission to Vm {1} ", getAdUserId(), getVmId());
                        setActionReturnValue(vdcReturnValueFromAddPerm);
                        return;
                    }
                    log.infoFormat("Vm {0} was attached to user {1} ", getVmId(), getAdUserId());
                } else {
                    log.infoFormat("No free Vms in pool {0}. Cannot allocate for user {1} ", getVmPoolId(), getAdUserId());
                    throw new VdcBLLException(VdcBllErrors.NO_FREE_VM_IN_POOL);
                }
            }
        }

        CreateAllSnapshotsFromVmParameters tempVar = new CreateAllSnapshotsFromVmParameters(getVmId(),
                "SnapshotForVmFromPool");
        tempVar.setShouldBeLogged(false);
        tempVar.setParentCommand(getParameters().getParentCommand() != VdcActionType.Unknown ? getParameters()
                .getParentCommand() : VdcActionType.AttachUserToVmFromPoolAndRun);
        tempVar.setSessionId(getParameters().getSessionId());
        tempVar.setEntityId(getParameters().getEntityId());
        CreateAllSnapshotsFromVmParameters p = tempVar;
        VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(
                VdcActionType.CreateAllSnapshotsFromVm, p, (CommandContext) getCompensationContext());

        getParameters().getImagesParameters().add(p);

        getTaskIdList().addAll(vdcReturnValue.getInternalTaskIdList());
        setSucceeded(vdcReturnValue.getSucceeded());
        setActionReturnValue(getVmId());

        getReturnValue().getTaskIdList().addAll(getReturnValue().getInternalTaskIdList());
    }

    @Override
    protected void EndSuccessfully() {
        // we are setting 'Vm' since VmId is overriden and 'Vm' is null
        // (since 'Vm' is dependant on 'mVmId', which is not set here).
        setVm(DbFacade.getInstance().getVmDAO().getById(getVmId()));

        if (getVm() != null) {
            // next line is for retrieving the VmPool from the DB
            // so we won't get a log-deadlock because of the transaction.
            vm_pools vmPool = getVmPool();

            if (DbFacade.getInstance().getDiskImageDAO().getImageVmPoolMapByVmId(getVm().getId()).size() > 0) {
               setSucceeded(Backend.getInstance().endAction(VdcActionType.CreateAllSnapshotsFromVm,
                       getParameters().getImagesParameters().get(0), getCompensationContext()).getSucceeded());

                if (getSucceeded()) {
                    // ParametersCurrentUser =
                    // PoolUserParameters.ParametersCurrentUser,
                    RunVmParams tempVar = new RunVmParams(getVm().getId());
                    tempVar.setSessionId(getParameters().getSessionId());
                    tempVar.setUseVnc(getVm().getvm_type() == VmType.Server);
                    VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(VdcActionType.RunVm,
                            tempVar);

                    setSucceeded(vdcReturnValue.getSucceeded());
                } else {
                    log.warn("EndSuccessfully: EndAction of CreateAllSnapshotsFromVm failed, detaching user from Vm");
                    detachUserFromVmFromPool(); // just in case.
                    getReturnValue().setEndActionTryAgain(false);
                }
            }

            else
            // Pool-snapshot is gone (probably due to ProcessVmPoolOnStopVm
            // treatment) ->
            // no point in running the VM or trying to run again the EndAction
            // method:
            {
                log.warn("EndSuccessfully: No images were created for Vm, detaching user from Vm");
                detachUserFromVmFromPool(); // just in case.
                getReturnValue().setEndActionTryAgain(false);
            }
        } else {
            setCommandShouldBeLogged(false);
            log.warn("AttachUserToVmFromPoolAndRunCommand::EndSuccessfully: Vm is null - not performing full EndAction");
            setSucceeded(true);
        }
    }

    @Override
    protected void EndWithFailure() {
        // we are setting 'Vm' since VmId is overriden and 'Vm' is null
        // (since 'Vm' is dependant on 'mVmId', which is not set here).
        setVm(DbFacade.getInstance().getVmDAO().getById(getVmId()));

        // next line is for retrieving the VmPool (and Vm, implicitly) from
        // the DB so we won't get a log-deadlock because of the transaction.
        vm_pools vmPool = getVmPool();

        // From AttachUserToVmAndRunCommand
        Backend.getInstance().endAction(VdcActionType.CreateAllSnapshotsFromVm,
                getParameters().getImagesParameters().get(0), getCompensationContext());
        detachUserFromVmFromPool();
        setSucceeded(true);

    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_ATTACH_USER_TO_VM_FROM_POOL
                    : AuditLogType.USER_ATTACH_USER_TO_VM_FROM_POOL_FAILED;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_ATTACH_USER_TO_VM_FROM_POOL_FINISHED_SUCCESS
                    : AuditLogType.USER_ATTACH_USER_TO_VM_FROM_POOL_FINISHED_FAILURE;

        default:
            return AuditLogType.USER_ATTACH_USER_TO_VM_FROM_POOL_FINISHED_FAILURE;
        }
    }

    protected void detachUserFromVmFromPool() {
        // Detach user from vm from pool:
        if (!getAdUserId().equals(Guid.Empty)) {
            permissions perm = DbFacade
                    .getInstance()
                    .getPermissionDAO()
                    .getForRoleAndAdElementAndObject(
                            PredefinedRoles.ENGINE_USER.getId(), getAdUserId(),
                            getVmId());
            if (perm != null) {
                DbFacade.getInstance().getPermissionDAO().remove(perm.getId());
            }
        }
    }
}
