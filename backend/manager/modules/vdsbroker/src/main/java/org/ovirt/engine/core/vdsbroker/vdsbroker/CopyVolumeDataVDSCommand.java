package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.vdscommands.CopyVolumeDataVDSCommandParameters;

public class CopyVolumeDataVDSCommand<P extends CopyVolumeDataVDSCommandParameters> extends VdsBrokerCommand<P> {
    public CopyVolumeDataVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
//        log.info("-- executeVdsBrokerCommand: calling 'copyVolumeData'");
//
//        status = getBroker().copyData(buildLocationInfo(getParameters().getSrcInfo()),
//                buildLocationInfo(getParameters().getDstInfo()), getParameters().isCollapse());
//
//        proceedProxyReturnValue();

        Map<String, Object> map = new HashMap<>();
        map.put("code", EngineError.Done.getValue());
        map.put("message", "done");
        status = new StatusOnlyReturnForXmlRpc(Collections.<String, Object>singletonMap("status", map));
        proceedProxyReturnValue();
    }
//
//    private Map<?, ?> buildLocationInfo(LocationInfo info) {
//        return LocationInfoHelper.prepareLocationInfoForVdsCommand(info);
//    }
}
