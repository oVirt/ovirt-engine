package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class GetConsoleDevicesQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetConsoleDevicesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final List<VmDevice> consoleDevices = getVmDeviceDao().getVmDeviceByVmIdAndType(getParameters().getId(),
                VmDeviceGeneralType.CONSOLE);

        List<String> result = new ArrayList<>(consoleDevices.size());
        for (VmDevice v : consoleDevices) {
            result.add(v.getDevice());
        }

        getQueryReturnValue().setReturnValue(result);
    }

    protected VmDeviceDao getVmDeviceDao() {
        return getDbFacade().getVmDeviceDao();
    }

}
