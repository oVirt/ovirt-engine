package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.GetDiskAlignmentVDSCommandParameters;

public class GetDiskAlignmentVDSCommand<P extends GetDiskAlignmentVDSCommandParameters> extends VdsBrokerCommand<P> {
    private AlignmentScanReturnForXmlRpc diskAlignment;

    public GetDiskAlignmentVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        diskAlignment = getBroker().getDiskAlignment(getParameters().getVmId().toString(), getParameters().getDriveSpecs());
        ProceedProxyReturnValue();

        // At the moment we only check that all the partition are aligned.
        // In the future we might want to keep a list of the unaligned ones.
        setReturnValue(!diskAlignment.getAlignment().values().contains(false));
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return diskAlignment.mStatus;
    }
}
