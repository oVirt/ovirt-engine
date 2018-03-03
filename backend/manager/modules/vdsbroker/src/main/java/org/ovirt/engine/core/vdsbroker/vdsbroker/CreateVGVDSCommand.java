package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ovirt.engine.core.common.vdscommands.CreateVGVDSCommandParameters;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturn;

public class CreateVGVDSCommand<P extends CreateVGVDSCommandParameters> extends VdsBrokerCommand<P> {
    @Inject
    private VdsDao vdsDao;

    private OneUuidReturn result;

    public CreateVGVDSCommand(P parameters) {
        super(parameters);
    }

    @PostConstruct
    public void init() {
        setVdsAndVdsStatic(vdsDao.get(getParameters().getVdsId()));
    }

    @Override
    protected void executeVdsBrokerCommand() {

        String storageDomainId = getParameters().getStorageDomainId().toString();
        Set<String> deviceList = getParameters().getDeviceList();
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
