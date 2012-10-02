package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.bll.quota.StorageQuotaValidationParameter;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.SysPrepParams;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.vdscommands.CreateVmVDSCommandParameters;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class RunVmOnceCommand<T extends RunVmOnceParams> extends RunVmCommand<T> {
    public RunVmOnceCommand(T runVmParams) {
        super(runVmParams);
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = super.canDoAction();

        // the condition allows to get only user and password which are both set (even with empty string) or both aren't
        // set (null), the action will fail if only one of those parameters is null.
        if (returnValue
                && (getParameters().getSysPrepUserName() == null ^ getParameters().getSysPrepPassword() == null)) {
            addCanDoActionMessage(VdcBllMessages.VM_CANNOT_RUN_ONCE_WITH_ILLEGAL_SYSPREP_PARAM);
            returnValue = false;
        }

        return returnValue;
    }

    /**
     * Refresh the associated values of the VM boot parameters with the values from the command parameters. The method
     * is used when VM is reloaded from the DB while its parameters hasn't been persisted (e.g. when running 'as once')
     */
    @Override
    protected void refreshBootParameters(RunVmParams runVmParameters) {
        if (runVmParameters == null) {
            return;
        }

        getVm().setinitrd_url(runVmParameters.getinitrd_url());
        getVm().setkernel_url(runVmParameters.getkernel_url());
        getVm().setkernel_params(runVmParameters.getkernel_params());
        getVm().setCustomProperties(runVmParameters.getCustomProperties());
        getVm().setboot_sequence(runVmParameters.getBootSequence());
    }

    @Override
    protected CreateVmVDSCommandParameters initVdsCreateVmParams() {
        getVm().setRunOnce(true);
        CreateVmVDSCommandParameters createVmParams = super.initVdsCreateVmParams();
        SysPrepParams sysPrepParams = new SysPrepParams();
        RunVmOnceParams runOnceParams = getParameters();
        sysPrepParams.setSysPrepDomainName(runOnceParams.getSysPrepDomainName());
        sysPrepParams.setSysPrepUserName(runOnceParams.getSysPrepUserName());
        sysPrepParams.setSysPrepPassword(runOnceParams.getSysPrepPassword());
        createVmParams.setSysPrepParams(sysPrepParams);
        return createVmParams;
    }

    @Override
    public void reportCompleted() {
        ExecutionContext executionContext = getExecutionContext();
        executionContext.setShouldEndJob(true);
        boolean success =
                getParameters().getRunAndPause() && getVmDynamicDao().get(getVmId()).getstatus() == VMStatus.Paused;
        ExecutionHandler.endJob(executionContext, success);
    }

    @Override
    public boolean validateAndSetQuota() {
        if (isInternalExecution()) {
            return true;
        }
        boolean quotaAcc = super.validateAndSetQuota();
        if (!quotaAcc) {
            return false;
        }
        // Only if this is run-stateless mode we calculate storage quota.
        if (!Boolean.TRUE.equals(getParameters().getRunAsStateless())) {
            return quotaAcc;
        }

        return QuotaManager.getInstance().validateAndSetStorageQuota(getStoragePool(),
                getStorageQuotaListParameters(),
                getReturnValue().getCanDoActionMessages());

    }

    private List<StorageQuotaValidationParameter> getStorageQuotaListParameters() {
        List<StorageQuotaValidationParameter> list = new ArrayList<StorageQuotaValidationParameter>();
        for (DiskImage image : getVm().getDiskList()) {
            if (image.getQuotaId() != null) {
                list.add(new StorageQuotaValidationParameter(image.getQuotaId(),
                        image.getstorage_ids().get(0),
                        image.getActualSize()));
            }
        }
        return list;
    }

    @Override
    public void rollbackQuota() {
        super.rollbackQuota();
        QuotaManager.getInstance().decreaseStorageQuota(getStoragePool(), getStorageQuotaListParameters());
    }

}
