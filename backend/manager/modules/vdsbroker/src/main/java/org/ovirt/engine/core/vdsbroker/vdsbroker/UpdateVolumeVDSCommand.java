package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.utils.LocationInfoHelper;
import org.ovirt.engine.core.common.vdscommands.UpdateVolumeVDSCommandParameters;

public class UpdateVolumeVDSCommand<P extends UpdateVolumeVDSCommandParameters> extends VdsBrokerCommand<P> {
    public UpdateVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        log.info("-- executeVdsBrokerCommand: calling 'updateVolume'");

        status = getBroker().updateVolume(getParameters().getJobId().toString(),
                LocationInfoHelper.prepareLocationInfoForVdsCommand(getParameters().getVolumeInfo()),
                prepareVolumeAttributes());

        proceedProxyReturnValue();
    }

    private Map<?, ?> prepareVolumeAttributes() {
        Map<String, Object> attr = new HashMap<>();
        if (getParameters().getLegal() != null) {
            attr.put("legality", getParameters().getLegal() ? "LEGAL" : "ILLEGAL");
        }

        if (getParameters().getDescription() != null) {
            attr.put("description", getParameters().getDescription());
        }

        if (getParameters().getShared() != null) {
            if (Boolean.TRUE.equals(getParameters().getShared())) {
                attr.put("type", "SHARED");
            } else {
                throw createDefaultConcreteException("volume type can be only updated to SHARED");
            }
        }

        if (getParameters().getGeneration() != null) {
            attr.put("generation", getParameters().getGeneration());
        }

        return attr;
    }
}
