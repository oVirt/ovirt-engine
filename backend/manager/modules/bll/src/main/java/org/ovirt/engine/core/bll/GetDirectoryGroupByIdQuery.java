package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.aaa.AuthzUtils;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.common.queries.DirectoryIdQueryParameters;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;

public class GetDirectoryGroupByIdQuery<P extends DirectoryIdQueryParameters> extends QueriesCommandBase<P> {

    public GetDirectoryGroupByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final String directoryName = getParameters().getDomain();
        final String id = getParameters().getId();
        final ExtensionProxy authz = EngineExtensionsManager.getInstance().getExtensionByName(directoryName);
        if (authz == null) {
            getQueryReturnValue().setSucceeded(false);
        } else {
            final DirectoryGroup group = AuthzUtils.findGroupById(authz, id);
            getQueryReturnValue().setReturnValue(group);
        }
    }

}
