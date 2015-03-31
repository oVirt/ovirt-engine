package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.GetDiskAlignmentVDSCommandParameters;

public class GetDiskAlignmentVDSCommand<P extends GetDiskAlignmentVDSCommandParameters> extends VdsBrokerCommand<P> {
    private AlignmentScanReturnForXmlRpc diskAlignment;

    public GetDiskAlignmentVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        log.info("-- GetDiskAlignmentVDSCommand::executeVdsBrokerCommand: calling 'getDiskAlignment', parameters:");
        log.info("++ vmId={}", getParameters().getVmId());
        log.info("++ driveSpecs={}", getParameters().getDriveSpecs());

        diskAlignment = getBroker().getDiskAlignment(getParameters().getVmId().toString(), getParameters().getDriveSpecs());
        proceedProxyReturnValue();

        // At the moment we only check that all the partition are aligned.
        // In the future we might want to keep a list of the unaligned ones.
        setReturnValue(!diskAlignment.getAlignment().values().contains(false));
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return diskAlignment.getXmlRpcStatus();
    }
}
