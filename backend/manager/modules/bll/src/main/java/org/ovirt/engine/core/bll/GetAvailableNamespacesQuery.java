package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;

public class GetAvailableNamespacesQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {

    public GetAvailableNamespacesQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Map<String, List<String>> namespacesMap = (Map<String, List<String>>) SsoOAuthServiceUtils
                .getAvailableNamespaces(getSessionDataContainer().getSsoAccessToken(getParameters().getSessionId()))
                .get("result");
        if (namespacesMap != null) {
            namespacesMap.values().forEach(Collections::sort);
        }
        setReturnValue(namespacesMap == null ? Collections.emptyMap() : namespacesMap);
    }
}
