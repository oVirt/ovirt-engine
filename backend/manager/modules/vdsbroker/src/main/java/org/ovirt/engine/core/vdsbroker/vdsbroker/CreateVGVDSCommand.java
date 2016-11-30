package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.List;

import org.ovirt.engine.core.common.vdscommands.CreateVGVDSCommandParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturn;

public class CreateVGVDSCommand<P extends CreateVGVDSCommandParameters> extends VdsBrokerCommand<P> {
    private OneUuidReturn result;

    public CreateVGVDSCommand(P parameters) {
        super(parameters, DbFacade.getInstance().getVdsDao().get(parameters.getVdsId()));
    }

    @Override
    protected void executeVdsBrokerCommand() {

        String storageDomainId = getParameters().getStorageDomainId().toString();
        List<String> deviceList = getParameters().getDeviceList();
        String[] deviceArray = deviceList.toArray(new String[deviceList.size()]);
        boolean isForce = getParameters().isForce();

        result = getBroker().createVG(storageDomainId, deviceArray, isForce);

        proceedProxyReturnValue();
        setReturnValue(result.uuid);
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
