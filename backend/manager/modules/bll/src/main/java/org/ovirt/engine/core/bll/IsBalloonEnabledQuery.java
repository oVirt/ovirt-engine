package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class IsBalloonEnabledQuery <P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public IsBalloonEnabledQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VmDeviceDao dao = getDbFacade().getVmDeviceDao();
        getQueryReturnValue().setReturnValue(dao.isMemBalloonEnabled(getParameters().getId()));
    }
}
