package org.ovirt.engine.core.bll;

import java.util.HashMap;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.utils.pm.VdsFenceOptions;

public class GetAgentFenceOptionsQuery<P extends VdcQueryParametersBase> extends FenceQueryBase<P> {

    public GetAgentFenceOptionsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VdsFenceOptions options = new VdsFenceOptions();
        HashMap<String, HashMap<String, String>> map = options.getFencingOptionMappingMap();
        getQueryReturnValue().setReturnValue(map);
        getQueryReturnValue().setSucceeded(map.size() > 0);
    }
}
