package org.ovirt.engine.core.bll.aaa;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.DirectoryIdQueryParameters;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;

public class GetDirectoryUserByIdQuery<P extends DirectoryIdQueryParameters> extends QueriesCommandBase<P> {

    public GetDirectoryUserByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        String directoryName = getParameters().getDomain();
        String id = getParameters().getId();
        ExtensionProxy authz = EngineExtensionsManager.getInstance().getExtensionByName(directoryName);
        if (authz == null) {
            getQueryReturnValue().setSucceeded(false);
        } else {
            for (String namespace : getParameters().constainsNamespace() ? Arrays.asList(getParameters().getNamespace()) : authz.getContext().<List<String>> get(Authz.ContextKeys.AVAILABLE_NAMESPACES)) {
                DirectoryUser user = DirectoryUtils.findDirectoryUserById(authz, namespace, id, false, false);
                if (user != null) {
                    getQueryReturnValue().setReturnValue(user);
                    break;
                }
            }
        }
    }

}
