package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.LocationInfo;
import org.ovirt.engine.core.common.utils.LocationInfoHelper;
import org.ovirt.engine.core.common.vdscommands.SealDisksVDSCommandParameters;

public class SealDisksVDSCommand<P extends SealDisksVDSCommandParameters> extends VdsBrokerCommand<P> {

    private StatusOnlyReturn result;

    public SealDisksVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        result = getBroker().sealDisks(
                getParameters().getEntityId().toString(),
                getParameters().getJobId().toString(),
                getParameters().getStoragePoolId().toString(),
                getParameters().getImages().stream().map(this::mapToEndpoint).collect(Collectors.toList()));
        proceedProxyReturnValue();
    }

    private Map<String, Object> mapToEndpoint(LocationInfo locationInfo) {
        return LocationInfoHelper.prepareLocationInfoForVdsCommand(locationInfo);
    }

    @Override
    protected Status getReturnStatus() {
        return result.status;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return result;
    }

}
