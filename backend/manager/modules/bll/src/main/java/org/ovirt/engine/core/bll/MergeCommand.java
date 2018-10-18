package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MergeParameters;
import org.ovirt.engine.core.common.businessentities.VmBlockJob;
import org.ovirt.engine.core.common.businessentities.VmJobState;
import org.ovirt.engine.core.common.businessentities.VmJobType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.MergeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.monitoring.VmJobsMonitoring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InternalCommandAttribute
public class MergeCommand<T extends MergeParameters>
        extends CommandBase<T> {
    private static final Logger log = LoggerFactory.getLogger(MergeCommand.class);

    @Inject
    private VmJobsMonitoring vmJobsMonitoring;

    public MergeCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public void executeCommand() {
        boolean mergeRunning = false;
        try {
            VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.Merge,
                    createVDSParameters());

            if (vdsReturnValue.getSucceeded()) {
                Guid jobId = (Guid) vdsReturnValue.getReturnValue();
                persistBlockJobPlaceholder(jobId);
                getParameters().setVmJobId(jobId);
                persistCommand(getParameters().getParentCommand(), true);
                log.debug("Merge started successfully");
                mergeRunning = true;
            } else {
                log.error("Failed to start Merge on VDS");
            }
        } catch (EngineException e) {
            log.error("Engine exception thrown while sending merge command", e);
            if (e.getErrorCode() == EngineError.imageErr || e.getErrorCode() == EngineError.mergeErr) {
                // In this case, we are not certain whether merge is currently running or
                // whether one of the relevant volumes already removed from the chain. In these cases,
                // we want to verify the current state; therefore, we consider the merge to be running.
                mergeRunning = true;
            }
        } finally {
            if (mergeRunning) {
                setSucceeded(true);
            } else {
                setCommandStatus(CommandStatus.FAILED);
            }
        }
    }

    private VDSParametersBase createVDSParameters() {
        return new MergeVDSCommandParameters(
                getParameters().getVdsId(),
                getParameters().getVmId(),
                getParameters().getStoragePoolId(),
                getParameters().getStorageDomainId(),
                getParameters().getImageGroupId(),
                getParameters().getImageId(),
                getParameters().getBaseImage().getImageId(),
                getParameters().getTopImage().getImageId(),
                getParameters().getBandwidth());
    }

    private void persistBlockJobPlaceholder(Guid jobId) {
        VmBlockJob blockJob = new VmBlockJob();
        blockJob.setVmId(getParameters().getVmId());
        blockJob.setId(jobId);
        blockJob.setJobType(VmJobType.BLOCK);
        blockJob.setJobState(VmJobState.UNKNOWN);
        blockJob.setImageGroupId(getParameters().getImageGroupId());
        blockJob.setStartTime(System.nanoTime());
        vmJobsMonitoring.addJob(blockJob);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
    }

    @Override
    public CommandCallback getCallback() {
        return Injector.injectMembers(new MergeCommandCallback());
    }
}
