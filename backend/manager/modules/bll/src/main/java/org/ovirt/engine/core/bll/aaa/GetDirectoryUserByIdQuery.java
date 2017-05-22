package org.ovirt.engine.core.bll.aaa;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.DirectoryIdQueryParameters;

public class GetDirectoryUserByIdQuery<P extends DirectoryIdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private DirectoryUtils directoryUtils;

    public GetDirectoryUserByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Map<String, Object> response = findDirectoryUserById();
        if (!response.containsKey("result")) {
            getQueryReturnValue().setSucceeded(false);
        } else {
            Collection<DirectoryUser> users = directoryUtils.mapPrincipalRecordsToDirectoryUsers(
                    getParameters().getDomain(),
                    (Collection<ExtMap>) response.get("result"));
            if (!users.isEmpty()) {
                getQueryReturnValue().setReturnValue(users.iterator().next());
            }
        }
    }

    public Map<String, Object> findDirectoryUserById() {
        return SsoOAuthServiceUtils.findDirectoryUserById(
                getSessionDataContainer().getSsoAccessToken(getParameters().getSessionId()),
                getParameters().getDomain(),
                getParameters().getNamespace(),
                getParameters().getId(),
                false,
                false);
    }
}
