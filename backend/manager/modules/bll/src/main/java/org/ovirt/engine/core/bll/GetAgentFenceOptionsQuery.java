package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.utils.pm.VdsFenceOptions;

public class GetAgentFenceOptionsQuery<P extends VdcQueryParametersBase> extends FenceQueryBase<P> {

    public GetAgentFenceOptionsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VdsFenceOptions options = new VdsFenceOptions();
        ValueObjectMap map = options.getFencingOptionMappingMap();
        getQueryReturnValue().setReturnValue(map);
        getQueryReturnValue().setSucceeded(map.asMap().size() > 0);
    }
}
