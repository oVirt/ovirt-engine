package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ovirt.engine.core.common.vdscommands.MomPolicyVDSParameters;
import org.ovirt.engine.core.dao.VdsDao;

/**
 * Send variables that fine tune MoM policy to VDSM
 */
public class SetMOMPolicyParametersVDSCommand extends VdsBrokerCommand<MomPolicyVDSParameters> {
    @Inject
    private VdsDao vdsDao;

    public SetMOMPolicyParametersVDSCommand(MomPolicyVDSParameters parameters) {
        super(parameters);
    }

    @PostConstruct
    public void init() {
        setVdsAndVdsStatic(vdsDao.get(getParameters().getVdsId()));
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().setMOMPolicyParameters(initDeviceStructure());
        proceedProxyReturnValue();
    }
    protected Map<String, Object> initDeviceStructure() {
        Map<String, Object> deviceStruct = new HashMap<>();
        deviceStruct.put(VdsProperties.balloonEnabled, getParameters().isEnableBalloon());
        deviceStruct.put(VdsProperties.ksmEnabled, getParameters().isEnableKsm());
        deviceStruct.put(VdsProperties.ksmMergeAcrossNodes, getParameters().isKsmMergeAcrossNumaNodes());
        return deviceStruct;
    }
}
