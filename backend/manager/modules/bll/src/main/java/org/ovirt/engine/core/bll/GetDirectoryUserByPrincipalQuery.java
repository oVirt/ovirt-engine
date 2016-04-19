package org.ovirt.engine.core.bll;

import java.util.Collection;
import java.util.Map;

import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.bll.aaa.DirectoryUtils;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetDirectoryUserByPrincipalParameters;

public class GetDirectoryUserByPrincipalQuery<P extends GetDirectoryUserByPrincipalParameters> extends QueriesCommandBase<P> {

    public GetDirectoryUserByPrincipalQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    public GetDirectoryUserByPrincipalQuery(P parameters) {
        this(parameters, null);
    }

    @Override
    protected void executeQueryCommand() {
        Map<String, Object> response = SsoOAuthServiceUtils.fetchPrincipalRecord(
                getSessionDataContainer().getSsoAccessToken(getParameters().getSessionId()),
                getParameters().getAuthz(),
                getParameters().getPrincnipal(),
                false,
                false
        );
        ExtMap principalRecord = null;
        if (response.containsKey("result")) {
            Collection<ExtMap> records = (Collection<ExtMap>) response.get("result");
            if (!records.isEmpty()) {
                principalRecord = records.iterator().next();
            }
        }
        getQueryReturnValue().setReturnValue(
                DirectoryUtils.mapPrincipalRecordToDirectoryUser(
                        getParameters().getAuthz(),
                        principalRecord
                )
        );
    }

}
