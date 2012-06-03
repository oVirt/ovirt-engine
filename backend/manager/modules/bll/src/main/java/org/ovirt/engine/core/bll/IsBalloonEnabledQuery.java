package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.dao.VmDeviceDAO;

public class IsBalloonEnabledQuery <P extends GetVmByVmIdParameters> extends QueriesCommandBase<P> {

    public IsBalloonEnabledQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VmDeviceDAO dao = getDbFacade().getVmDeviceDAO();
        getQueryReturnValue().setReturnValue(dao.isMemBalloonEnabled(getParameters().getId()));
    }
}
