package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmDeviceDAO;

public class IsBalloonEnabledQuery <P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public IsBalloonEnabledQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VmDeviceDAO dao = getDbFacade().getVmDeviceDao();
        getQueryReturnValue().setReturnValue(dao.isMemBalloonEnabled(getParameters().getId()));
    }
}
