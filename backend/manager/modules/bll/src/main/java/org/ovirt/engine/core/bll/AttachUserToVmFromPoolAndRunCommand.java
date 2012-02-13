package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmPoolUserParameters;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@LockIdNameAttribute(fieldName = "AdUserId")
public class AttachUserToVmFromPoolAndRunCommand<T extends VmPoolUserParameters> extends
        AttachUserToVmFromPoolCommand<T> {
    protected AttachUserToVmFromPoolAndRunCommand(Guid commandId) {
        super(commandId);
    }

    public AttachUserToVmFromPoolAndRunCommand(T parameters) {
        super(parameters);
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

        super.executeCommand();

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
                super.EndSuccessfully();

                if (getSucceeded()) {
                    // ParametersCurrentUser =
                    // PoolUserParameters.ParametersCurrentUser,
                    RunVmParams tempVar = new RunVmParams(getVm().getId());
                    tempVar.setSessionId(getParameters().getSessionId());
                    tempVar.setUseVnc(getVm().getvm_type() == VmType.Server);
                    VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(VdcActionType.RunVm,
                            tempVar);

                    setSucceeded(vdcReturnValue.getSucceeded());
                }
            }

            else
            // Pool-snapshot is gone (probably due to ProcessVmPoolOnStopVm
            // treatment) ->
            // no point in running the VM or trying to run again the EndAction
            // method:
            {
                DetachUserFromVmFromPool(); // just in case.
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

        super.EndWithFailure();
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

    private static Log log = LogFactory.getLog(AttachUserToVmFromPoolAndRunCommand.class);
}
