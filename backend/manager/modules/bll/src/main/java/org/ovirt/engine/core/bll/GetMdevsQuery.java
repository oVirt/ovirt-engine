package org.ovirt.engine.core.bll;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmMdevType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class GetMdevsQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmDeviceDao vmDeviceDao;

    public GetMdevsQuery(P parameters, EngineContext context) {
        super(parameters, context);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                vmDeviceDao.getVmDeviceByVmIdAndType(
                        getParameters().getId(),
                        VmDeviceGeneralType.MDEV)
                .stream()
                .map(device -> new VmMdevType(device))
                .collect(Collectors.toList()));
    }
}
