package org.ovirt.engine.core.bll;

import java.util.HashMap;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.utils.pm.VdsFenceOptions;

public class GetAgentFenceOptions2Query<P extends VdcQueryParametersBase> extends FenceQueryBase<P> {

    public GetAgentFenceOptions2Query(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VdsFenceOptions options = new VdsFenceOptions();
        HashMap<String, HashMap<String, String>> map = options.getFencingOptionMappingMap2();
        getQueryReturnValue().setReturnValue(map);
        getQueryReturnValue().setSucceeded(map.size() > 0);
    }
}
