package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.NbdServerVDSParameters;

public class StartNbdServerVDSCommand<P extends NbdServerVDSParameters> extends VdsBrokerCommand<P> {

    private NbdServerURLReturn nbdServerURLReturn;

    public StartNbdServerVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        nbdServerURLReturn = getBroker().startNbdServer(
                getParameters().getServerId().toString(),
                createNbdServerConfigMap());
        proceedProxyReturnValue();
        setReturnValue(nbdServerURLReturn.getNBDServerURL());
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return nbdServerURLReturn;
    }

    @Override
    protected Status getReturnStatus() {
        return nbdServerURLReturn.getStatus();
    }

    private Map<String, Object> createNbdServerConfigMap() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("sd_id", getParameters().getStorageDomainId().toString());
        configMap.put("img_id", getParameters().getImageId().toString());
        configMap.put("vol_id", getParameters().getVolumeId().toString());
        configMap.put("readonly", getParameters().isReadonly());
        configMap.put("discard", getParameters().isDiscard());
        // Vdsm < 4.5 do not recognize this option and ignores it.
        configMap.put("detect_zeroes", getParameters().isDetectZeroes());
        configMap.put("backing_chain", getParameters().getBackingChain());
        configMap.put("bitmap", getParameters().getBitmap() != null ? getParameters().getBitmap().toString() : null);
        return configMap;
    }
}
