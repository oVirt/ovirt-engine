package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
import org.ovirt.engine.core.aaa.AuthzUtils;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.common.queries.DirectoryIdQueryParameters;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;

public class GetDirectoryUserByIdQuery<P extends DirectoryIdQueryParameters> extends QueriesCommandBase<P> {

    public GetDirectoryUserByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        String directoryName = getParameters().getDomain();
        String id = getParameters().getId();
        ExtensionProxy authz = AuthenticationProfileRepository.getInstance().getAuthz(directoryName);
        if (authz == null) {
            getQueryReturnValue().setSucceeded(false);
        } else {
            DirectoryUser user = AuthzUtils.findPrincipalById(authz, id);
            getQueryReturnValue().setReturnValue(user);
        }
    }

}
