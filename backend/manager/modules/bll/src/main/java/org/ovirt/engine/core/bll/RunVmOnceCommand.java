package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.SysPrepParams;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.CreateVmVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.OsRepositoryImpl;

@NonTransactiveCommandAttribute
public class RunVmOnceCommand<T extends RunVmOnceParams> extends RunVmCommand<T> implements QuotaStorageDependent {
    public RunVmOnceCommand(T runVmParams) {
        super(runVmParams);
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        // the condition allows to get only user and password which are both set (even with empty string) or both aren't
        // set (null), the action will fail if only one of those parameters is null.
        if (getParameters().getSysPrepUserName() == null ^ getParameters().getSysPrepPassword() == null) {
            return failCanDoAction(VdcBllMessages.VM_CANNOT_RUN_ONCE_WITH_ILLEGAL_SYSPREP_PARAM);
        }

        if ((getParameters().getVmInit() != null && !OsRepositoryImpl.INSTANCE.isWindows(getVm().getOs()))
                && !FeatureSupported.cloudInit(getVm().getVdsGroupCompatibilityVersion())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLOUD_INIT_IS_NOT_SUPPORTED);
        }

        return true;
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

        getVm().setInitrdUrl(runVmParameters.getInitrdUrl());
        getVm().setKernelUrl(runVmParameters.getKernelUrl());
        getVm().setKernelParams(runVmParameters.getKernelParams());
        getVm().setCustomProperties(runVmParameters.getCustomProperties());

        getVm().setBootSequence((runVmParameters.getBootSequence() != null) ?
                runVmParameters.getBootSequence() :
                getVm().getDefaultBootSequence());
    }

    @Override
    protected CreateVmVDSCommandParameters initCreateVmParams() {
        CreateVmVDSCommandParameters createVmParams = super.initCreateVmParams();
        createVmParams.getVm().setRunOnce(true);

        RunVmOnceParams runOnceParams = getParameters();

        SysPrepParams sysPrepParams = new SysPrepParams();
        sysPrepParams.setSysPrepDomainName(runOnceParams.getSysPrepDomainName());
        sysPrepParams.setSysPrepUserName(runOnceParams.getSysPrepUserName());
        sysPrepParams.setSysPrepPassword(runOnceParams.getSysPrepPassword());
        createVmParams.setSysPrepParams(sysPrepParams);

        createVmParams.setVmInit(runOnceParams.getVmInit());

        return createVmParams;
    }

    @Override
    protected void endExecutionMonitoring() {
        ExecutionContext executionContext = getExecutionContext();
        executionContext.setShouldEndJob(true);
        boolean runAndPausedSucceeded =
                Boolean.TRUE.equals(getParameters().getRunAndPause())
                        && getVmDynamicDao().get(getVmId()).getStatus() == VMStatus.Paused;
        ExecutionHandler.endJob(executionContext, runAndPausedSucceeded);
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

    @Override
    protected List<Guid> getVdsWhiteList() {
        VDS destinationVds = getDestinationVds();
        if (destinationVds != null) {
            return Arrays.asList(destinationVds.getId());
        }
        return super.getVdsWhiteList();
    }

    @Override
    protected void initVm() {
        super.initVm();

        if (getParameters().getVncKeyboardLayout() == null) {
            getVm().getDynamicData().setVncKeyboardLayout(getVm().getDefaultVncKeyboardLayout());
        } else {
            // if is not null it means runVM was launch from the run once command, thus
            // the VM can run with keyboard layout type which is different from its default display type
            getVm().getDynamicData().setVncKeyboardLayout(getParameters().getVncKeyboardLayout());
        }
    }

}
