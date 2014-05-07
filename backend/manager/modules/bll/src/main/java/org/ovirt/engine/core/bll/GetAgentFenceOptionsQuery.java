package org.ovirt.engine.core.bll;

import java.util.HashMap;

import org.ovirt.engine.core.common.queries.GetAgentFenceOptionsQueryParameters;
import org.ovirt.engine.core.utils.pm.VdsFenceOptions;

public class GetAgentFenceOptionsQuery<P extends GetAgentFenceOptionsQueryParameters> extends FenceQueryBase<P> {

    public GetAgentFenceOptionsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        String version = getParameters().getVersion();
        VdsFenceOptions options = new VdsFenceOptions(version);
        HashMap<String, HashMap<String, String>> map = options.getFenceOptionMappingMap();
        getQueryReturnValue().setReturnValue(map);
        getQueryReturnValue().setSucceeded(map.size() > 0);
    }
}
