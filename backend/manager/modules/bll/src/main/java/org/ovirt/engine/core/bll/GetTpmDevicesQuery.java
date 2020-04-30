package org.ovirt.engine.core.bll;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class GetTpmDevicesQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmDeviceDao vmDeviceDao;

    public GetTpmDevicesQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                vmDeviceDao.getVmDeviceByVmIdAndType(
                        getParameters().getId(),
                        VmDeviceGeneralType.TPM)
                .stream()
                .map(v -> v.getDevice())
                .collect(Collectors.toList()));
    }
}

