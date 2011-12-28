package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmPoolUserParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * Command to attach a VM from a pool to a user, so that the user can use the attached VM.<br>
 * The command-level lock is per user, so that a single user won't be able to attach more than one VM from a given pool
 * since this is the policy.
 */
@LockIdNameAttribute(fieldName = "AdUserId")
@InternalCommandAttribute
public class AttachUserToVmFromPoolCommand<T extends VmPoolUserParameters> extends VmPoolUserCommandBase<T> {
    protected AttachUserToVmFromPoolCommand(Guid commandId) {
        super(commandId);
    }

    public AttachUserToVmFromPoolCommand(T parameters) {
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

        // VmId = GetVmToAttach(PoolUserParameters.VmPoolId);
        // Parameters.EntityId = VmId;
        synchronized (_lockObject) {
            if (GetVmToAttach(getParameters().getVmPoolId()).equals(Guid.Empty)) // no
                                                                                 // available
                                                                                 // VMs:
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
    protected void executeCommand() {
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
                    Backend.getInstance().runInternalAction(VdcActionType.AddPermission,
                            permParams,
                            getCompensationContext());
                    log.infoFormat("Vm {0} was attached to user {1} ", getVmId(), getAdUserId());
                }
            }
        }

        if (getVmId().equals(Guid.Empty)) {
            log.infoFormat("No free Vms in pool. Cannot allocate for user {1} ", getAdUserId());
            throw new VdcBLLException(VdcBllErrors.NO_FREE_VM_IN_POOL);
        }
        CreateAllSnapshotsFromVmParameters tempVar = new CreateAllSnapshotsFromVmParameters(getVmId(),
                "SnapshotForVmFromPool");
        tempVar.setShouldBeLogged(false);
        tempVar.setParentCommand(getParameters().getParentCommand() != VdcActionType.Unknown ? getParameters()
                .getParentCommand() : VdcActionType.AttachUserToVmFromPool);
        tempVar.setSessionId(getParameters().getSessionId());
        tempVar.setEntityId(getParameters().getEntityId());
        CreateAllSnapshotsFromVmParameters p = tempVar;
        VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(
                VdcActionType.CreateAllSnapshotsFromVm, p, getCompensationContext());

        getParameters().getImagesParameters().add(p);

        getTaskIdList().addAll(vdcReturnValue.getInternalTaskIdList());
        setSucceeded(vdcReturnValue.getSucceeded());
        setActionReturnValue(getVmId());
    }

    @Override
    protected void EndSuccessfully() {
        Backend.getInstance().endAction(VdcActionType.CreateAllSnapshotsFromVm,
                getParameters().getImagesParameters().get(0), getCompensationContext());

        setSucceeded(true);
    }

    @Override
    protected void EndWithFailure() {
        Backend.getInstance().endAction(VdcActionType.CreateAllSnapshotsFromVm,
                getParameters().getImagesParameters().get(0), getCompensationContext());
        DetachUserFromVmFromPool();
        setSucceeded(true);
    }

    protected void DetachUserFromVmFromPool() {
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

    private static LogCompat log = LogFactoryCompat.getLog(AttachUserToVmFromPoolCommand.class);
}
