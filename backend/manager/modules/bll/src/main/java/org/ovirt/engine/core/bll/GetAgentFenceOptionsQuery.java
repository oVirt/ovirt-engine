package org.ovirt.engine.core.bll;

import java.util.Map;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetAgentFenceOptionsQueryParameters;
import org.ovirt.engine.core.utils.pm.VdsFenceOptions;

public class GetAgentFenceOptionsQuery<P extends GetAgentFenceOptionsQueryParameters> extends FenceQueryBase<P> {

    public GetAgentFenceOptionsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        String version = getParameters().getVersion();
        VdsFenceOptions options = new VdsFenceOptions(version);
        Map<String, Map<String, String>> map = options.getFenceOptionMappingMap();
        getQueryReturnValue().setReturnValue(map);
        getQueryReturnValue().setSucceeded(map.size() > 0);
    }
}
