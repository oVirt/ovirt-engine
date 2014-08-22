package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.aaa.AuthzUtils;
import org.ovirt.engine.core.bll.aaa.DirectoryUtils;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetDirectoryUserByPrincipalParameters;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;

public class GetDirectoryUserByPrincipalQuery<P extends GetDirectoryUserByPrincipalParameters> extends QueriesCommandBase<P> {

    public GetDirectoryUserByPrincipalQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    public GetDirectoryUserByPrincipalQuery(P parameters) {
        this(parameters, null);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DirectoryUtils.mapPrincipalRecordToDirectoryUser(
                        getParameters().getAuthz(),
                        AuthzUtils.fetchPrincipalRecord(
                                EngineExtensionsManager.getInstance().getExtensionByName(
                                        getParameters().getAuthz()
                                        ),
                                getParameters().getPrincnipal()
                        )
                )
        );
    }

}
