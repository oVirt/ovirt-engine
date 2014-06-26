package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.SysPrepParams;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.CreateVmVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.utils.OsRepositoryImpl;

@LockIdNameAttribute
@NonTransactiveCommandAttribute
public class RunVmOnceCommand<T extends RunVmOnceParams> extends RunVmCommand<T> implements QuotaStorageDependent {

    public RunVmOnceCommand(T runVmParams, CommandContext commandContext) {
        super(runVmParams, commandContext);

        // Load payload if user didn't send via run-once
        if (getParameters().getVmPayload() == null) {
            loadPayload();
        }
    }


    public RunVmOnceCommand(T runVmParams) {
        this(runVmParams, null);
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

        if (getParameters().getVmInit() != null) {
            if (!OsRepositoryImpl.INSTANCE.isWindows(getVm().getOs()) &&
                    !FeatureSupported.cloudInit(getVm().getVdsGroupCompatibilityVersion())) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLOUD_INIT_IS_NOT_SUPPORTED);
            }

            if (getParameters().getVmInit().isPasswordAlreadyStored()) {
                VmBase temp = new VmBase();
                temp.setId(getParameters().getVmId());
                VmHandler.updateVmInitFromDB(temp, false);
                getParameters().getVmInit().setRootPassword(temp.getVmInit().getRootPassword());
            }
        }

        return true;
    }

    private void loadPayload() {
        VmDeviceDAO dao = getDbFacade().getVmDeviceDao();
        List<VmDevice> disks = dao.getVmDeviceByVmIdAndType(getParameters().getVmId(), VmDeviceGeneralType.DISK);

        for (VmDevice disk : disks) {
            if (VmPayload.isPayload(disk.getSpecParams())) {
                VmPayload payload = new VmPayload(VmDeviceType.valueOf(disk.getType().name()),
                        disk.getSpecParams());
                payload.setType(VmDeviceType.valueOf(disk.getDevice().toUpperCase()));
                getVm().setVmPayload(payload);
                break;
            }
        }
    }

    @Override
    protected Guid getPredefinedVdsIdToRunOn() {
        // destination VDS ID has priority over the dedicated VDS
        return getParameters().getDestinationVdsId() != null ?
            getParameters().getDestinationVdsId()
            : super.getPredefinedVdsIdToRunOn();
    }

    /**
     * Refresh the associated values of the VM boot parameters with the values from the command parameters. The method
     * is used when VM is reloaded from the DB while its parameters hasn't been persisted (e.g. when running 'as once')
     */
    @Override
    protected void refreshBootParameters(RunVmParams runVmParameters) {
        getVm().setInitrdUrl(runVmParameters.getInitrdUrl());
        getVm().setKernelUrl(runVmParameters.getKernelUrl());
        getVm().setKernelParams(runVmParameters.getKernelParams());
        getVm().setCustomProperties(runVmParameters.getCustomProperties());

        getVm().setBootSequence((runVmParameters.getBootSequence() != null) ?
                runVmParameters.getBootSequence() :
                getVm().getDefaultBootSequence());

        getVm().setRunOnce(true);
    }

    @Override
    protected CreateVmVDSCommandParameters buildCreateVmParameters() {
        CreateVmVDSCommandParameters createVmParams = super.buildCreateVmParameters();

        RunVmOnceParams runOnceParams = getParameters();

        SysPrepParams sysPrepParams = new SysPrepParams();
        sysPrepParams.setSysPrepDomainName(runOnceParams.getSysPrepDomainName());
        sysPrepParams.setSysPrepUserName(runOnceParams.getSysPrepUserName());
        sysPrepParams.setSysPrepPassword(runOnceParams.getSysPrepPassword());
        createVmParams.setSysPrepParams(sysPrepParams);

        createVmParams.getVm().setVmInit(getParameters().getVmInit());

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
        if (isRunAsStateless()) {
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
        Guid predefinedDestinationVdsId = getPredefinedVdsIdToRunOn();
        return predefinedDestinationVdsId != null ?
                Arrays.asList(predefinedDestinationVdsId)
                : super.getVdsWhiteList();
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

        if (getParameters().getVmInit() != null) {
            getVm().setVmInit(getParameters().getVmInit());
        }
    }

    @Override
    protected boolean isVmRunningOnNonDefaultVds() {
        return getParameters().getDestinationVdsId() == null
                && super.isVmRunningOnNonDefaultVds();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        final List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();

        // check, if user can override default target host for VM
        if (getVm() != null) {
            final Guid destinationVdsId = getParameters().getDestinationVdsId();
            if (destinationVdsId != null && !destinationVdsId.equals(getVm().getDedicatedVmForVds())) {
                permissionList.add(new PermissionSubject(getParameters().getVmId(),
                    VdcObjectType.VM,
                    ActionGroup.EDIT_ADMIN_VM_PROPERTIES));
            }
        }

        return permissionList;
    }
}
