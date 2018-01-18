package org.ovirt.engine.core.bll.exportimport;

import java.io.File;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.ConvertVmParameters;
import org.ovirt.engine.core.common.businessentities.V2VJobInfo.JobStatus;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.ConvertVmVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.ovirt.engine.core.di.Injector;
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
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

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
            cachedCallback = Injector.injectMembers(new ConvertVmCallback(getCommandId()));
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

    ///////////////////
    //// Sync Part ////
    ///////////////////

    @Override
    protected boolean validate() {
        if (getVds() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_VDS_IN_POOL);
        }

        if (getVds().getStatus() != VDSStatus.Up) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL);
        }

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
        parameters.setCompatVersion(getParameters().getCompatVersion());
        return parameters;
    }

    protected String getVirtioIsoPath() {
        return getParameters().getVirtioIsoName() == null ? null :
            new File(getIsoPrefix(getStoragePoolId(), getVdsId()), getParameters().getVirtioIsoName()).getPath();
    }

    ////////////////////
    //// Async Part ////
    ////////////////////

    @Override
    protected void endWithFailure() {
        try {
            getVdsManager().removeV2VJobInfoForVm(getVmId());
            getVmManager().setConvertProxyHostId(null);
            setSucceeded(true);
        } finally {
            deleteV2VJob();
        }
    }

    protected void endSuccessfully() {
        getVdsManager().removeV2VJobInfoForVm(getVmId());
        getVmManager().setConvertProxyHostId(null);
        setSucceeded(true);
    }

    private void deleteV2VJob() {
        runVdsCommand(VDSCommandType.DeleteV2VJob,
                new VdsAndVmIDVDSParametersBase(getVdsId(), getVmId()));
    }

    /////////////////////////
    //// Utility Methods ////
    /////////////////////////

    protected VmManager getVmManager() {
        return resourceManager.getVmManager(getVmId());
    }

    protected VdsManager getVdsManager() {
        return resourceManager.getVdsManager(getVdsId());
    }

    private CommandExecutionStatus getCommandExecutionStatus() {
        return commandCoordinatorUtil.getCommandExecutionStatus(getCommandId());
    }
}
