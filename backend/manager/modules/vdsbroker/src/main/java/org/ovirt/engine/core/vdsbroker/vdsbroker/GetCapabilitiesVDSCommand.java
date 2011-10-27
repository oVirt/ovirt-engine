package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;

@Logged(executionLevel = LogLevel.DEBUG)
public class GetCapabilitiesVDSCommand<P extends VdsIdAndVdsVDSCommandParametersBase> extends InfoVdsBrokerCommand<P> {
    public GetCapabilitiesVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
        if (getVds() == null) {
            setVds(DbFacade.getInstance().getVdsDAO().get(parameters.getVdsId()));
            parameters.setVds(getVds());
        }
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        infoReturn = getBroker().getCapabilities();
        ProceedProxyReturnValue();
        VdsBrokerObjectsBuilder.updateVDSDynamicData(getVds(), infoReturn.mInfo);

    }
}
