package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.SysPrepParams;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.CreateVmVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDeviceDao;

@NonTransactiveCommandAttribute
public class RunVmOnceCommand<T extends RunVmOnceParams> extends RunVmCommand<T> implements QuotaStorageDependent {

    public RunVmOnceCommand(T runVmParams, CommandContext commandContext) {
        super(runVmParams, commandContext);

        // Load payload if user didn't send via run-once
        if (getParameters().getVmPayload() == null) {
            loadPayload();
        }

        /* if run-once was used then the dynamic vm fields must be updated before running the scheduler filters (which are called via super.Validate->runVmValidator) */
        earlyUpdateVmDynamicRunOnce();
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        // the condition allows to get only user and password which are both set (even with empty string) or both aren't
        // set (null), the action will fail if only one of those parameters is null.
        if (getParameters().getSysPrepUserName() == null ^ getParameters().getSysPrepPassword() == null) {
            return failValidation(EngineMessage.VM_CANNOT_RUN_ONCE_WITH_ILLEGAL_SYSPREP_PARAM);
        }

        if (getParameters().getVmInit() != null) {
            if (getParameters().getVmInit().isPasswordAlreadyStored()) {
                VmBase temp = new VmBase();
                temp.setId(getParameters().getVmId());
                VmHandler.updateVmInitFromDB(temp, false);
                getParameters().getVmInit().setRootPassword(temp.getVmInit().getRootPassword());
            }
        }

        return true;
    }

    /**
     * The function updates the dynamic vm fields with the run-once parameters for fields which are accessed during
     * the validation process and thus must be updated at an earlier stage than the initVm() stage.
     */
    private void earlyUpdateVmDynamicRunOnce() {
        if(getVm() != null) { // function is called before super.validate(), vm object is not safe yet
            if (getParameters().getCustomCpuName() != null) {
                getVm().setCpuName(getParameters().getCustomCpuName());
            }

            if (getParameters().getCustomEmulatedMachine() != null) {
                getVm().setEmulatedMachine(getParameters().getCustomEmulatedMachine());
            }
        }

    }

    private void loadPayload() {
        VmDeviceDao dao = getDbFacade().getVmDeviceDao();
        List<VmDevice> disks = dao.getVmDeviceByVmIdAndType(getParameters().getVmId(), VmDeviceGeneralType.DISK);

        for (VmDevice disk : disks) {
            if (VmPayload.isPayload(disk.getSpecParams())) {
                VmPayload payload = new VmPayload(disk);
                getVm().setVmPayload(payload);
                break;
            }
        }
    }

    @Override
    protected List<Guid> getPredefinedVdsIdListToRunOn() {
        // destination VDS ID has priority over the dedicated VDS
        if (getParameters().getDestinationVdsId() != null){
            List<Guid> destIdList = new LinkedList<>();
            destIdList.add(getParameters().getDestinationVdsId());
            return destIdList;
        }
        return super.getPredefinedVdsIdListToRunOn();
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

        if (getParameters().getVmInit() != null) {
            createVmParams.getVm().setVmInit(getParameters().getVmInit());
        }

        return createVmParams;
    }

    /**
     * This methods sets graphics infos of a VM to correspond to graphics set in Run Once.
     */
    @Override
    protected void updateGraphicsInfos() {
        if (getParameters().getRunOnceGraphics().isEmpty()) {
            // configure from DB
            super.updateGraphicsInfos();
        } else {
            // configure from params
            for (GraphicsType graphicsType : getParameters().getRunOnceGraphics()) {
                getVm().getGraphicsInfos().put(graphicsType, new GraphicsInfo());
            }
        }
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
        List<QuotaConsumptionParameter> list = new ArrayList<>();

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
        if (getPredefinedVdsIdListToRunOn().size() > 0){
            return getPredefinedVdsIdListToRunOn();
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
            if (destinationVdsId != null &&
                getVm().getDedicatedVmForVdsList().contains(destinationVdsId) == false) {
                permissionList.add(new PermissionSubject(getParameters().getVmId(),
                    VdcObjectType.VM,
                    ActionGroup.EDIT_ADMIN_VM_PROPERTIES));
            }
        }

        return permissionList;
    }
}
