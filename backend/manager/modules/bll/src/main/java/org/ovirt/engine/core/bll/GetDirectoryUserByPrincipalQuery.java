package org.ovirt.engine.core.bll;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.bll.aaa.DirectoryUtils;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetDirectoryUserByPrincipalParameters;

public class GetDirectoryUserByPrincipalQuery<P extends GetDirectoryUserByPrincipalParameters> extends QueriesCommandBase<P> {
    @Inject
    private DirectoryUtils directoryUtils;

    public GetDirectoryUserByPrincipalQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
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
                directoryUtils.mapPrincipalRecordToDirectoryUser(
                        getParameters().getAuthz(),
                        principalRecord
                )
        );
    }

}
