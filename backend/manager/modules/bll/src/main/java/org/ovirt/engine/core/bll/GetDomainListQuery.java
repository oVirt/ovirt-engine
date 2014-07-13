package org.ovirt.engine.core.bll;

import static java.util.Collections.sort;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.aaa.AuthzUtils;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.extensions.mgr.ExtensionsManager;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;

public class GetDomainListQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetDomainListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // Get the list of authentication profile names:
        List<ExtensionProxy> extensions =
                getExtensionsManager().getExtensionsByService(Authz.class.getName());
        List<String> names = new ArrayList<>(extensions.size());
        for (ExtensionProxy extension : extensions) {
            names.add(AuthzUtils.getName(extension));
        }
        sort(names);

        // Return the sorted list:
        getQueryReturnValue().setReturnValue(names);
    }

    protected ExtensionsManager getExtensionsManager() {
        return EngineExtensionsManager.getInstance();
    }
}
