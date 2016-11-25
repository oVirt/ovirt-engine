package org.ovirt.engine.core.vdsbroker.vdsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.SpmStatusResult;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.HSMTaskGuidBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SpmStartVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SpmStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ThreadUtils;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturn;

public class SpmStartVDSCommand<P extends SpmStartVDSCommandParameters> extends VdsBrokerCommand<P> {

    @Inject
    private ResourceManager resourceManager;

    public SpmStartVDSCommand(P parameters) {
        super(parameters);
        vdsId = parameters.getVdsId();
    }

    private OneUuidReturn result;
    private Guid vdsId;

    @Override
    protected void executeVdsBrokerCommand() {
        result = getBroker().spmStart(getParameters().getStoragePoolId().toString(),
                    getParameters().getPrevId(), getParameters().getPrevLVER(),
                    getParameters().getRecoveryMode().getValue(),
                    String.valueOf(getParameters().getSCSIFencing()).toLowerCase(),
                    Config.getValue(ConfigValues.MaxNumberOfHostsInStoragePool), getParameters().getStoragePoolFormatType().getValue());
        proceedProxyReturnValue();
        Guid taskId = new Guid(result.uuid);

        AsyncTaskStatus taskStatus;
        log.info("spmStart polling started: taskId '{}'", taskId);
        do {
            // TODO: make configurable
            ThreadUtils.sleep(1000L);
            taskStatus = (AsyncTaskStatus) resourceManager.runVdsCommand(VDSCommandType.HSMGetTaskStatus,
                    new HSMTaskGuidBaseVDSCommandParameters(vdsId, taskId)).getReturnValue();
            log.debug("spmStart polling - task status: '{}'", taskStatus.getStatus());
        } while (taskStatus.getStatus() != AsyncTaskStatusEnum.finished
                && taskStatus.getStatus() != AsyncTaskStatusEnum.unknown);

        log.info("spmStart polling ended: taskId '{}' task status '{}'", taskId, taskStatus.getStatus());

        if (!taskStatus.getTaskEndedSuccessfully()) {
            log.error("Start SPM Task failed - result: '{}', message: {}", taskStatus.getResult(),
                    taskStatus.getMessage());
        }
        SpmStatusResult spmStatus = (SpmStatusResult) resourceManager.runVdsCommand(VDSCommandType.SpmStatus,
                new SpmStatusVDSCommandParameters(vdsId, getParameters().getStoragePoolId()))
                .getReturnValue();
        if (spmStatus != null) {
            log.info("spmStart polling ended, spm status: {}", spmStatus.getSpmStatus());
        } else {
            log.error("spmStart polling ended, failed to get the spm status");
        }
        try {
            resourceManager.runVdsCommand(VDSCommandType.HSMClearTask,
                    new HSMTaskGuidBaseVDSCommandParameters(vdsId, taskId));
        } catch (Exception e) {
            log.error("Could not clear spmStart task '{}', continuing with SPM selection: {}", taskId, e.getMessage());
            log.debug("Exception", e);
        }
        setReturnValue(spmStatus);
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return result;
    }
}
