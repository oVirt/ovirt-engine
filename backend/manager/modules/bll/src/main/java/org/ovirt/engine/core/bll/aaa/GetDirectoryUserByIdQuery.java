package org.ovirt.engine.core.bll.aaa;

import java.util.Collection;
import java.util.Map;

import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.DirectoryIdQueryParameters;

public class GetDirectoryUserByIdQuery<P extends DirectoryIdQueryParameters> extends QueriesCommandBase<P> {

    public GetDirectoryUserByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Map<String, Object> response = findDirectoryUserById();
        if (!response.containsKey("result")) {
            getQueryReturnValue().setSucceeded(false);
        } else {
            Collection<DirectoryUser> users = DirectoryUtils.mapPrincipalRecordsToDirectoryUsers(
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
