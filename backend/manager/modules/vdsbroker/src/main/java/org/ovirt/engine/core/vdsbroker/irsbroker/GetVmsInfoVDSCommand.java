package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.vdscommands.GetVmsInfoVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

@Logged(returnLevel = LogLevel.TRACE)
public class GetVmsInfoVDSCommand<P extends GetVmsInfoVDSCommandParameters> extends IrsBrokerCommand<P> {
    private GetVmsInfoReturn _vmsInfo;

    public GetVmsInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        String storagePoolId = getParameters().getStoragePoolId().toString();
        String storageDomainId = getParameters().getStorageDomainId().toString();
        ArrayList<String> ids = new ArrayList<>();

        if (getParameters().getVmIdList() != null) {
            for (Guid id : getParameters().getVmIdList()) {
                ids.add(id.toString());
            }
        }

        _vmsInfo = getIrsProxy().getVmsInfo(storagePoolId, storageDomainId, ids.toArray(new String[] {}));
        proceedProxyReturnValue();

        Map<String, Object> struct = _vmsInfo.vmlist;
        ArrayList<String> retVal = new ArrayList<>();
        for (Entry<String, Object> entry : struct.entrySet()) {
            retVal.add(entry.getValue().toString());
        }
        setReturnValue(retVal);

    }

    @Override
    protected Status getReturnStatus() {
        return _vmsInfo.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _vmsInfo;
    }
}
