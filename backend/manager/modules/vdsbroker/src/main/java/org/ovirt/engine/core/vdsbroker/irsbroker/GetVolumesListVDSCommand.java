package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.ArrayList;

import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.vdscommands.StoragePoolDomainAndGroupIdBaseVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class GetVolumesListVDSCommand<P extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters> extends IrsBrokerCommand<P> {
    private UUIDListReturn _result;

    public GetVolumesListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        _result =
                getIrsProxy().getVolumesList(getParameters().getStorageDomainId().toString(),
                        getParameters().getStoragePoolId().toString(),
                        getParameters().getImageGroupId().toString());
        proceedProxyReturnValue();
        ArrayList<Guid> tempRetValue = new ArrayList<>(_result.getUUIDList().length);
        for (String id : _result.getUUIDList()) {
            tempRetValue.add(new Guid(id));
        }
        setReturnValue(tempRetValue);
    }

    @Override
    protected Status getReturnStatus() {
        return _result.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }

    @Override
    protected void proceedProxyReturnValue() {
        EngineError returnStatus = getReturnValueFromStatus(getReturnStatus());
        switch (returnStatus) {
        case GetStorageDomainListError:
            getVDSReturnValue().setVdsError(new VDSError(returnStatus, getReturnStatus().message));
            getVDSReturnValue().setSucceeded(false);
            break;
        default:
            super.proceedProxyReturnValue();
            break;
        }
    }

}
