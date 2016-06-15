package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.MomPolicyVDSParameters;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * Send variables that fine tune MoM policy to VDSM
 */
public class SetMOMPolicyParametersVDSCommand extends VdsBrokerCommand<MomPolicyVDSParameters> {

    public SetMOMPolicyParametersVDSCommand(MomPolicyVDSParameters parameters) {
        super(parameters, DbFacade.getInstance().getVdsDao().get(parameters.getVdsId()));
    }

    @Override
    protected void executeVdsBrokerCommand() {
        // Do not do anything when the Host's compatibility level is too old
        if (getVds().getVdsGroupCompatibilityVersion().compareTo(Version.v3_3) >= 0) {
            status = getBroker().setMOMPolicyParameters(initDeviceStructure());
            proceedProxyReturnValue();
        }
    }
    protected Map<String, Object> initDeviceStructure() {
        Map<String, Object> deviceStruct = new HashMap<>();
        deviceStruct.put(VdsProperties.balloonEnabled, getParameters().isEnableBalloon());
        deviceStruct.put(VdsProperties.ksmEnabled, getParameters().isEnableKsm());

        if (getVds().getVdsGroupCompatibilityVersion().compareTo(Version.v3_6) >= 0) {
            deviceStruct.put(VdsProperties.ksmMergeAcrossNodes, getParameters().isKsmMergeAcrossNumaNodes());
        }

        return deviceStruct;
    }
}
