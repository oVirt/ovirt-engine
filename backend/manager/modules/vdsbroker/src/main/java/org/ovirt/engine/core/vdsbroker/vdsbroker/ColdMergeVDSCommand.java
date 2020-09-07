package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.utils.SubchainInfoHelper;
import org.ovirt.engine.core.common.vdscommands.ColdMergeVDSCommandParameters;

public class ColdMergeVDSCommand<P extends ColdMergeVDSCommandParameters> extends VdsBrokerCommand<P> {

    public ColdMergeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        log.info("-- executeVdsBrokerCommand: calling 'mergeSubchain'");

        status = getBroker().mergeSubchain(getParameters().getJobId().toString(),
                SubchainInfoHelper.prepareSubchainInfoForVdsCommand(getParameters().getSubchainInfo()),
                getParameters().isMergeBitmaps());

        proceedProxyReturnValue();
    }
}
