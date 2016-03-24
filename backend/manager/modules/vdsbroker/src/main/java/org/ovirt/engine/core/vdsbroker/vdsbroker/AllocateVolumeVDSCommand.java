package org.ovirt.engine.core.vdsbroker.vdsbroker;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.vdscommands.AllocateVolumeVDSCommandParameters;

public class AllocateVolumeVDSCommand<P extends AllocateVolumeVDSCommandParameters> extends StorageJobVDSCommand<P> {
    public AllocateVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
//        log.info("-- executeJobCommand: calling 'allocateVolume'");
//        status = getBroker().allocateVolume(
//                getParameters().getStoragePoolId().toString(),
//                getParameters().getStorageDomainId().toString(),
//                getParameters().getImageGroupId().toString(),
//                getParameters().getVolumeId().toString(),
//                Long.valueOf(getParameters().getSizeInBytes()).toString());
//
//        proceedProxyReturnValue();

        Map<String, Object> map = new HashMap<>();
        map.put("code", EngineError.Done.getValue());
        map.put("message", "done");
        status = new StatusOnlyReturnForXmlRpc(Collections.<String, Object>singletonMap("status", map));
        proceedProxyReturnValue();
    }
}
