package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.MergeVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

/**
 * Live Merge the specified snapshots.
 */
public class MergeVDSCommand<P extends MergeVDSCommandParameters> extends VdsBrokerCommand<P> {
    public MergeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        Guid taskId = Guid.newGuid();

        status = getBroker().merge(
                getParameters().getVmId().toString(),
                getParameters().getDriveSpecs(),
                getParameters().getBaseImageId().toString(),
                getParameters().getTopImageId().toString(),
                String.valueOf(getParameters().getBandwidth()),
                taskId.toString());

        proceedProxyReturnValue();
        if (getVDSReturnValue().getSucceeded()) {
            setReturnValue(taskId);
        }
    }
}
