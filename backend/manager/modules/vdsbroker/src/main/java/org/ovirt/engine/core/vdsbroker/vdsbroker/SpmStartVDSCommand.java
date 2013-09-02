package org.ovirt.engine.core.vdsbroker.vdsbroker;

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
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturnForXmlRpc;

public class SpmStartVDSCommand<P extends SpmStartVDSCommandParameters> extends VdsBrokerCommand<P> {
    public SpmStartVDSCommand(P parameters) {
        super(parameters);
        vdsId = parameters.getVdsId();
    }

    private OneUuidReturnForXmlRpc _result;
    private Guid vdsId;

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().spmStart(getParameters().getStoragePoolId().toString(),
                    getParameters().getPrevId(), getParameters().getPrevLVER(),
                    getParameters().getRecoveryMode().getValue(),
                    String.valueOf(getParameters().getSCSIFencing()).toLowerCase(),
                    Config.<Integer> GetValue(ConfigValues.MaxNumberOfHostsInStoragePool), getParameters().getStoragePoolFormatType().getValue());
        proceedProxyReturnValue();
        Guid taskId = new Guid(_result.mUuid);

        AsyncTaskStatus taskStatus;
        log.infoFormat("spmStart polling started: taskId = {0}", taskId);
        do {
            // TODO: make configurable
            ThreadUtils.sleep(1000);
            taskStatus = (AsyncTaskStatus) ResourceManager
                    .getInstance()
                    .runVdsCommand(VDSCommandType.HSMGetTaskStatus,
                            new HSMTaskGuidBaseVDSCommandParameters(vdsId, taskId)).getReturnValue();
            log.debugFormat("spmStart polling - task status: {0}", taskStatus.getStatus().toString());
        } while (taskStatus.getStatus() != AsyncTaskStatusEnum.finished
                && taskStatus.getStatus() != AsyncTaskStatusEnum.unknown);

        log.infoFormat("spmStart polling ended: taskId = {0} task status = {1}", taskId, taskStatus.getStatus());

        if (!taskStatus.getTaskEndedSuccessfully()) {
            log.errorFormat("Start SPM Task failed - result: {0}, message: {1}", taskStatus.getResult().toString(),
                    taskStatus.getMessage());
        }
        SpmStatusResult spmStatus = (SpmStatusResult) ResourceManager
                .getInstance()
                .runVdsCommand(VDSCommandType.SpmStatus,
                        new SpmStatusVDSCommandParameters(vdsId, getParameters().getStoragePoolId()))
                .getReturnValue();
        if (spmStatus != null) {
            log.infoFormat("spmStart polling ended, spm status: {0}", spmStatus.getSpmStatus().toString());
        } else {
            log.errorFormat("spmStart polling ended, failed to get the spm status");
        }
        try {
            ResourceManager.getInstance().runVdsCommand(VDSCommandType.HSMClearTask,
                    new HSMTaskGuidBaseVDSCommandParameters(vdsId, taskId));
        } catch (java.lang.Exception e) {
            log.errorFormat("Could not clear spmStart task (id - {0}), continuing with SPM selection.", taskId);
        }
        setReturnValue(spmStatus);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }
}
