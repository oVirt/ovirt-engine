package org.ovirt.engine.core.bll;

import java.util.HashMap;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.utils.pm.VdsFencingOptions;

public class GetAgentFenceOptions2Query<P extends VdcQueryParametersBase> extends FencingQueryBase<P> {

    public GetAgentFenceOptions2Query(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VdsFencingOptions options = new VdsFencingOptions();
        HashMap<String, HashMap<String, String>> map = options.getFencingOptionMappingMap2();
        getQueryReturnValue().setReturnValue(map);
        getQueryReturnValue().setSucceeded(map.size() > 0);
    }
}
