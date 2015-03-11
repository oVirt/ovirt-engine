package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallBack;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MergeParameters;
import org.ovirt.engine.core.common.businessentities.VmBlockJob;
import org.ovirt.engine.core.common.businessentities.VmJobState;
import org.ovirt.engine.core.common.businessentities.VmJobType;
import org.ovirt.engine.core.common.vdscommands.MergeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InternalCommandAttribute
public class MergeCommand<T extends MergeParameters>
        extends CommandBase<T> {
    private static final Logger log = LoggerFactory.getLogger(MergeCommand.class);

    public MergeCommand(T parameters) {
        super(parameters);
    }

    public MergeCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public void executeCommand() {
        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.Merge,
                createVDSParameters());

        if (vdsReturnValue.getSucceeded()) {
            Guid jobId = (Guid) vdsReturnValue.getReturnValue();
            persistBlockJobPlaceholder(jobId);
            getParameters().setVmJobId(jobId);
            // setSucceeded to indicate executeCommand success; doPolling will check commandStatus
            setSucceeded(true);
            persistCommand(getParameters().getParentCommand(), true);
            log.debug("Merge started successfully");
        } else {
            log.error("Failed to start Merge on VDS");
            setCommandStatus(CommandStatus.FAILED);
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
        DbFacade.getInstance().getVmJobDao().save(blockJob);
        log.info("Stored placeholder for job id '{}'", blockJob.getId());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
    }

    @Override
    public CommandCallBack getCallback() {
        return new MergeCommandCallback();
    }
}
