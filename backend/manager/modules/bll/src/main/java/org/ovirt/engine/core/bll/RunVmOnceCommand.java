package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
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
import org.ovirt.engine.core.common.vdscommands.CreateVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.CloudInitHandler;

@NonTransactiveCommandAttribute
public class RunVmOnceCommand<T extends RunVmOnceParams> extends RunVmCommand<T> implements QuotaStorageDependent {

    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private CloudInitHandler cloudInitHandler;

    public RunVmOnceCommand(T runVmParams, CommandContext commandContext) {
        super(runVmParams, commandContext);
    }

    @Override
    protected void init() {
        if (getVm() == null) {
            return;
        }

        super.init();
        String vmClusterCpuName;
        // use cluster value if the compatibility versions of vm and cluster are the same
        if (getCluster().getCompatibilityVersion().equals(getVm().getCompatibilityVersion())) {
            vmClusterCpuName = getCluster().getCpuVerb();
        } else {
         // use configured value if the compatibility versions of vm and cluster are different
            vmClusterCpuName = getCpuFlagsManagerHandler().getCpuId(
                    getVm().getClusterCpuName(),
                    getVm().getCompatibilityVersion());
        }

        if (getParameters().getCustomCpuName() != null && !getParameters().getCustomCpuName().equals(
                getVm().getCustomCpuName() != null ?
                        getVm().getCustomCpuName() :
                        vmClusterCpuName)) {
            // The user overrode CPU name, drop the CPU passthrough flags
            // and do what the user wanted.
            getVm().setUseHostCpuFlags(false);

            getVm().setCustomCpuName(getParameters().getCustomCpuName());
        }
        if (getParameters().getCustomEmulatedMachine() != null) {
            getVm().setEmulatedMachine(getParameters().getCustomEmulatedMachine());
        }
        if (getParameters().getBootMenuEnabled() != null) {
            getVm().setBootMenuEnabled(getParameters().getBootMenuEnabled());
        }
        if (getParameters().getSpiceFileTransferEnabled() != null) {
            getVm().setSpiceFileTransferEnabled(getParameters().getSpiceFileTransferEnabled());
        }
        if (getParameters().getSpiceCopyPasteEnabled() != null) {
            getVm().setSpiceCopyPasteEnabled(getParameters().getSpiceCopyPasteEnabled());
        }
        if (getParameters().getBootSequence() != null) {
            getVm().setBootSequence(getParameters().getBootSequence());
        }
        getVm().setVolatileRun(getParameters().isVolatileRun());
        getVm().setInitrdUrl(getParameters().getInitrdUrl());
        getVm().setKernelUrl(getParameters().getKernelUrl());
        getVm().setKernelParams(getParameters().getKernelParams());
        getVm().setCustomProperties(getParameters().getCustomProperties());
        getVm().setRunOnce(true);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected boolean validateImpl() {
        if (!super.validateImpl()) {
            return false;
        }

        // the condition allows to get only user and password which are both set (even with empty string) or both aren't
        // set (null), the action will fail if only one of those parameters is null.
        if (getParameters().getSysPrepUserName() == null ^ getParameters().getSysPrepPassword() == null) {
            return failValidation(EngineMessage.VM_CANNOT_RUN_ONCE_WITH_ILLEGAL_SYSPREP_PARAM);
        }

        if (getParameters().getVmInit() != null && getParameters().getVmInit().isPasswordAlreadyStored()) {
            VmBase temp = new VmBase();
            temp.setId(getParameters().getVmId());
            vmHandler.updateVmInitFromDB(temp, false);
            getParameters().getVmInit().setRootPassword(temp.getVmInit().getRootPassword());
        }

        List<EngineMessage> msgs = cloudInitHandler.validate(getParameters().getVmInit());
        if (!CollectionUtils.isEmpty(msgs)) {
            return failValidation(msgs);
        }

        return true;
    }

    @Override
    protected void loadPayload() {
        List<VmDevice> disks = vmDeviceDao.getVmDeviceByVmIdAndType(getParameters().getVmId(), VmDeviceGeneralType.DISK);
        for (VmDevice disk : disks) {
            if (VmPayload.isPayload(disk.getSpecParams())) {
                vmPayload = new VmPayload(disk);
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

    @Override
    protected CreateVDSCommandParameters buildCreateVmParameters() {
        CreateVDSCommandParameters createVmParams = super.buildCreateVmParameters();
        createVmParams.setVolatileRun(getParameters().isVolatileRun());

        SysPrepParams sysPrepParams = new SysPrepParams();
        sysPrepParams.setSysPrepDomainName(getParameters().getSysPrepDomainName());
        sysPrepParams.setSysPrepUserName(getParameters().getSysPrepUserName());
        sysPrepParams.setSysPrepPassword(getParameters().getSysPrepPassword());
        createVmParams.setSysPrepParams(sysPrepParams);

        if (getParameters().getVmInit() != null) {
            createVmParams.getVm().setVmInit(getParameters().getVmInit());
        }
        return createVmParams;
    }

    /**
     * This method sets graphics & video devices info of a VM to correspond to graphics set & display type in Run Once.
     */
    @Override
    protected void updateGraphicsAndDisplayInfos() {
        // graphics devices
        if (getParameters().getRunOnceGraphics().isEmpty() && getParameters().getRunOnceDisplayType() == null) {
            // configure from DB
            super.updateGraphicsAndDisplayInfos();
        } else {
            // configure from params
            for (GraphicsType graphicsType : getParameters().getRunOnceGraphics()) {
                getVm().getGraphicsInfos().put(graphicsType, new GraphicsInfo());
            }
        }
        // video devices
        getVm().setDefaultDisplayType(getParameters().getRunOnceDisplayType());
    }

    @Override
    protected void endExecutionMonitoring() {
        ExecutionContext executionContext = getExecutionContext();
        executionContext.setShouldEndJob(true);
        boolean runAndPausedSucceeded =
                Boolean.TRUE.equals(getParameters().getRunAndPause())
                        && vmDynamicDao.get(getVmId()).getStatus() == VMStatus.Paused;
        executionHandler.endJob(executionContext, runAndPausedSucceeded);
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();

        //if runAsStateless
        if (isRunAsStateless()) {
            for (DiskImage image : getVm().getDiskList()) {
                list.add(new QuotaStorageConsumptionParameter(image.getQuotaId(),
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

        // special permission is needed to change custom properties
        if (!StringUtils.isEmpty(getParameters().getCustomProperties())) {
            permissionList.add(new PermissionSubject(getParameters().getVmId(),
                VdcObjectType.VM,
                ActionGroup.CHANGE_VM_CUSTOM_PROPERTIES));
        }

        // check, if user can override default target host for VM
        if (getVm() != null) {
            final Guid destinationVdsId = getParameters().getDestinationVdsId();
            if (destinationVdsId != null && !getVm().getDedicatedVmForVdsList().contains(destinationVdsId)) {
                permissionList.add(new PermissionSubject(getParameters().getVmId(),
                    VdcObjectType.VM,
                    ActionGroup.EDIT_ADMIN_VM_PROPERTIES));
            }
        }

        return permissionList;
    }

}
