package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.GetVolumeInfoVDSCommandParameters;

public class GetVolumeInfoVDSCommand<P extends GetVolumeInfoVDSCommandParameters> extends VdsBrokerCommand<P> {
    private VolumeInfoReturnForXmlRpc result;

    public GetVolumeInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        GetVolumeInfoVDSCommandParameters params = getParameters();
        result = getBroker().getVolumeInfo(
                params.getStorageDomainId().toString(),
                params.getStoragePoolId().toString(),
                params.getImageGroupId().toString(),
                params.getImageId().toString());
        proceedProxyReturnValue();
        // Not setting the return value for now, as this is only needed to verify
        // volume existence.  It can be filled in when a consumer needs the info.
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return result.getXmlRpcStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return result;
    }

}
