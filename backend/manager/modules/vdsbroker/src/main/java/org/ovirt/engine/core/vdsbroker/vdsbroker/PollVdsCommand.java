package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.concurrent.Future;

import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.vdsbroker.FutureVdsCommand;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcRunTimeException;

/**
 * Sole purpose of this command is to check connectivity with VDSM by invoking getCapabilities verb. Generally it is
 * used on network provisioning flows where VDSM is committing network changes only when traffic is from backend is
 * detected (configurable behavior)
 */
public class PollVdsCommand<P extends VdsIdAndVdsVDSCommandParametersBase> extends VdsBrokerCommand<P> implements FutureVdsCommand {

    private Future<StatusOnlyReturnForXmlRpc> httpTask;
    private Future<VDSReturnValue> vdsmTask;

    public PollVdsCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        httpTask = getBroker().poll();
        vdsmTask = new FutureVdsmTask<FutureVdsCommand>(this);
    }

    @Override
    public Future<VDSReturnValue> getTask() {
        return vdsmTask;
    }

    @Override
    public Future<StatusOnlyReturnForXmlRpc> getHttpTask() {
        return httpTask;
    }

    @Override
    public void proccessReturnValue() throws XmlRpcRunTimeException {
        ProceedProxyReturnValue();
    }

    @Override
    public VDSReturnValue getRetVal() {
        return getVDSReturnValue();
    }

    @Override
    public void execute() {
        ExecuteVdsBrokerCommand();
    }

}
