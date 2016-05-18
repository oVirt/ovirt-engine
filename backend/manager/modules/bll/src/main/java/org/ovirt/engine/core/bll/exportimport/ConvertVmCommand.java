package org.ovirt.engine.core.bll.exportimport;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ConvertVmParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.V2VJobInfo.JobStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.ConvertVmVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndPoolIDVDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.VmManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisableInPrepareMode
@NonTransactiveCommandAttribute
public class ConvertVmCommand<T extends ConvertVmParameters> extends VmCommand<T> {
    private static final Logger log = LoggerFactory.getLogger(ConvertVmCommand.class);

    @Inject
    private ResourceManager resourceManager;

    private ConvertVmCallback cachedCallback;

    public ConvertVmCommand(Guid commandId) {
        super(commandId);
    }

    public ConvertVmCommand(T parameters, CommandContext context) {
        super(parameters, context);
    }

    @Override
    protected void init() {
        super.init();
        setVmName(getParameters().getVmName());
        setVdsId(getParameters().getProxyHostId());
        setClusterId(getParameters().getClusterId());
        setStoragePoolId(getParameters().getStoragePoolId());
        setStorageDomainId(getParameters().getStorageDomainId());
    }

    @Override
    public CommandCallback getCallback() {
        if (cachedCallback == null) {
            cachedCallback = new ConvertVmCallback(getCommandId());
            // if the callback is created after the command was executed, it means that the engine restarted
            // so there is no v2v-job in vdsManager and thus we add a new job with unknown status there
            if (getCommandExecutionStatus() == CommandExecutionStatus.EXECUTED) {
                monitorV2VJob(JobStatus.UNKNOWN);
            }
        }
        return cachedCallback;
    }

    private void monitorV2VJob(JobStatus initialJobStatus) {
        getVdsManager().addV2VJobInfoForVm(getVmId(), initialJobStatus);
        getVmManager().setConvertProxyHostId(getVdsId());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(
                        LockingGroup.VM,
                        getVmIsBeingImportedMessage()));
    }

    protected String getVmIsBeingImportedMessage() {
        StringBuilder builder = new StringBuilder(EngineMessage.ACTION_TYPE_FAILED_VM_IS_BEING_IMPORTED.name());
        if (getVmName() != null) {
            builder.append(String.format("$VmName %1$s", getVmName()));
        }
        return builder.toString();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded()?
                    AuditLogType.IMPORTEXPORT_STARTING_CONVERT_VM
                    : AuditLogType.IMPORTEXPORT_IMPORT_VM_FAILED;
        case END_SUCCESS:
            return getSucceeded()?
                    AuditLogType.IMPORTEXPORT_IMPORT_VM
                    : AuditLogType.IMPORTEXPORT_IMPORT_VM_FAILED;
        case END_FAILURE:
            return AuditLogType.IMPORTEXPORT_IMPORT_VM_FAILED;
        }
        return super.getAuditLogTypeValue();
    }

    ///////////////////
    //// Sync Part ////
    ///////////////////

    @Override
    protected boolean validate() {
        if (getVds() != null && getVds().getStatus() != VDSStatus.Up) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL);
        }

        if (getVds() == null && !selectProxyHost()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_VDS_IN_POOL);
        }

        return true;
    }

    private boolean selectProxyHost() {
        List<VDS> activeHosts = getVdsDao().getAllForStoragePoolAndStatus(getStoragePoolId(), VDSStatus.Up);
        if (activeHosts.isEmpty()) {
            return false;
        }
        VDS activeHost = activeHosts.get(0);
        setVds(activeHost);
        // update the parameters for the end-action phase
        getParameters().setProxyHostId(activeHost.getId());
        return true;
    }

    @Override
    protected void executeVmCommand() {
        try {
            VDSReturnValue retValue = runVdsCommand();
            if (retValue.getSucceeded()) {
                monitorV2VJob(JobStatus.WAIT_FOR_START);
                setSucceeded(true);
            } else {
                log.error("Failed to convert VM");
                setCommandStatus(CommandStatus.FAILED);
            }
        } catch (EngineException e) {
            log.error("Failed to convert VM", e);
            setCommandStatus(CommandStatus.FAILED);
        }
    }

    protected VDSReturnValue runVdsCommand() {
        return runVdsCommand(
                VDSCommandType.ConvertVm,
                buildConvertParameters());
    }

    private ConvertVmVDSParameters buildConvertParameters() {
        ConvertVmVDSParameters parameters = new ConvertVmVDSParameters(getVdsId());
        parameters.setUrl(getParameters().getUrl());
        parameters.setUsername(getParameters().getUsername());
        parameters.setPassword(getParameters().getPassword());
        parameters.setDisks(getParameters().getDisks());
        parameters.setVmId(getVmId());
        parameters.setVmName(getVmName());
        parameters.setStoragePoolId(getStoragePoolId());
        parameters.setStorageDomainId(getStorageDomainId());
        parameters.setVirtioIsoPath(getVirtioIsoPath());
        return parameters;
    }

    protected String getVirtioIsoPath() {
        return getParameters().getVirtioIsoName() == null ? null :
            new File(getIsoPrefix(), getParameters().getVirtioIsoName()).getPath();
    }

    private String getIsoPrefix() {
        return (String) runVdsCommand(VDSCommandType.IsoPrefix,
                new VdsAndPoolIDVDSParametersBase(getVdsId(), getStoragePoolId())).getReturnValue();
    }

    ////////////////////
    //// Async Part ////
    ////////////////////

    @Override
    protected void endSuccessfully() {
        getReturnValue().setEndActionTryAgain(false);
        try {
            if (getParameters().getOriginType() != OriginType.KVM) {
                VM vm = readVmFromOvf(getOvfOfConvertedVm());
                updateBootDiskFlag(vm);
                addImportedDevices(vm);
            }
            setSucceeded(true);
        } catch (EngineException e) {
            log.info("failed to add devices to converted vm");
            removeVm();
        } finally {
            deleteV2VJob();
        }
    }

    @Override
    protected void endWithFailure() {
        auditLog(this, AuditLogType.IMPORTEXPORT_CONVERT_FAILED);
        removeVm();
        deleteV2VJob();
        setSucceeded(true);
    }

    private VM readVmFromOvf(String ovf) {
        try {
            return new OvfHelper().readVmFromOvf(ovf);
        } catch (OvfReaderException e) {
            log.debug("failed to parse a given ovf configuration: \n " + ovf, e);
            auditLog(this, AuditLogType.IMPORTEXPORT_INVALID_OVF);
            throw new EngineException();
        }
    }

    private String getOvfOfConvertedVm() {
        VDSReturnValue retValue = runVdsCommand(
                VDSCommandType.GetConvertedOvf,
                new VdsAndVmIDVDSParametersBase(getVdsId(), getVmId()));
        if (!retValue.getSucceeded()) {
            auditLog(this, AuditLogType.IMPORTEXPORT_CANNOT_GET_OVF);
            throw new EngineException();
        }
        return (String) retValue.getReturnValue();
    }

    private void deleteV2VJob() {
        getVdsManager().removeV2VJobInfoForVm(getVmId());
        getVmManager().setConvertProxyHostId(null);
        runVdsCommand(
                VDSCommandType.DeleteV2VJob,
                new VdsAndVmIDVDSParametersBase(getVdsId(), getVmId()));
    }

    private void updateBootDiskFlag(VM vm) {
        vm.getStaticData().getImages().stream().filter(disk -> disk.getDiskVmElementForVm(vm.getId()).isBoot())
                .forEach(disk -> getDiskVmElementDao().update(disk.getDiskVmElementForVm(vm.getId())));
    }

    private void addImportedDevices(VM vm) {
        VmStatic vmStatic = vm.getStaticData();
        // Disk and network interface devices were already added
        vmStatic.setImages(new ArrayList<>());
        vmStatic.setInterfaces(new ArrayList<>());
        ImportUtils.updateGraphicsDevices(vmStatic, getStoragePool().getCompatibilityVersion());
        VmDeviceUtils.addImportedDevices(vmStatic, false);
        saveDiskVmElements(vm);
        getVmDeviceDao().updateBootOrderInBatch(new ArrayList<>(vm.getManagedVmDeviceMap().values()));
    }

    private void saveDiskVmElements(VM vm) {
        for (DiskImage disk : vm.getStaticData().getImages()) {
            getDiskVmElementDao().save(disk.getDiskVmElementForVm(vm.getId()));
        }
    }

    private void removeVm() {
        runInternalAction(
                VdcActionType.RemoveVm,
                new RemoveVmParameters(getVmId(), true));
    }

    /////////////////////////
    //// Utility Methods ////
    /////////////////////////

    protected VmManager getVmManager() {
        return getResourceManager().getVmManager(getVmId());
    }

    protected VdsManager getVdsManager() {
        return getResourceManager().getVdsManager(getVdsId());
    }

    protected ResourceManager getResourceManager() {
        return resourceManager;
    }

    private CommandExecutionStatus getCommandExecutionStatus() {
        return CommandCoordinatorUtil.getCommandExecutionStatus(getCommandId());
    }
}
