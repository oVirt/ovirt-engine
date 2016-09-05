package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarkPoolInReconstructModeVDSCommand<P extends IrsBaseVDSCommandParameters>
        extends IrsBrokerCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(MarkPoolInReconstructModeVDSCommand.class);

    public MarkPoolInReconstructModeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVDSCommand() {
        try {
            IrsProxy proxy = getCurrentIrsProxy();
            proxy.clearPoolTimers();
            proxy.clearCache();
        } catch (Exception e) {
            log.error("Could not change timers for pool '{}': {}", getParameters().getStoragePoolId(), e.getMessage());
            log.debug("Exception", e);
        }
        getVDSReturnValue().setSucceeded(true);
    }
}
