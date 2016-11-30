package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.GetFileStatsParameters;
import org.ovirt.engine.core.vdsbroker.irsbroker.FileStatsReturn;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;

public class GetFileStatsVDSCommand<P extends GetFileStatsParameters> extends IrsBrokerCommand<P> {

    private FileStatsReturn fileStats;

    public GetFileStatsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        fileStats = getIrsProxy().getFileStats(getParameters().getSdUUID().toString(),
                getParameters().getPattern(), getParameters().isCaseSensitive());

        proceedProxyReturnValue();
        setReturnValue(fileStats.getFileStats());
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return fileStats;
    }

    @Override
    protected Status getReturnStatus() {
        return fileStats.getStatus();
    }
}
