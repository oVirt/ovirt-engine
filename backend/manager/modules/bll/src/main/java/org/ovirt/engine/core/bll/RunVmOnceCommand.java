package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.SysPrepParams;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.CreateVmVDSCommandParameters;

public class RunVmOnceCommand<T extends RunVmOnceParams> extends RunVmCommand<T> implements QuotaStorageDependent {
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

        getVm().setInitrdUrl(runVmParameters.getinitrd_url());
        getVm().setKernelUrl(runVmParameters.getkernel_url());
        getVm().setKernelParams(runVmParameters.getkernel_params());
        getVm().setCustomProperties(runVmParameters.getCustomProperties());

        getVm().setBootSequence((runVmParameters.getBootSequence() != null) ?
                runVmParameters.getBootSequence() :
                getVm().getDefaultBootSequence());
    }

    @Override
    protected CreateVmVDSCommandParameters initCreateVmParams() {
        getVm().setRunOnce(true);
        CreateVmVDSCommandParameters createVmParams = super.initCreateVmParams();
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
                getParameters().getRunAndPause() && getVmDynamicDao().get(getVmId()).getStatus() == VMStatus.Paused;
        ExecutionHandler.endJob(executionContext, success);
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();

        //if runAsStateless
        if (Boolean.TRUE.equals(getParameters().getRunAsStateless())) {
            for (DiskImage image : getVm().getDiskList()) {
                list.add(new QuotaStorageConsumptionParameter(image.getQuotaId(), null,
                        QuotaConsumptionParameter.QuotaAction.CONSUME,
                        image.getStorageIds().get(0), image.getActualSize()));
            }
        }
        return list;
    }
}
