package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
import org.ovirt.engine.core.aaa.AuthzUtils;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.common.queries.DirectoryIdQueryParameters;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;

public class GetDirectoryGroupByIdQuery<P extends DirectoryIdQueryParameters> extends QueriesCommandBase<P> {

    public GetDirectoryGroupByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final String directoryName = getParameters().getDomain();
        final String id = getParameters().getId();
        final ExtensionProxy authz = AuthenticationProfileRepository.getInstance().getAuthz(directoryName);
        if (authz == null) {
            getQueryReturnValue().setSucceeded(false);
        } else {
            final DirectoryGroup group = AuthzUtils.findGroupById(authz, id);
            getQueryReturnValue().setReturnValue(group);
        }
    }

}
