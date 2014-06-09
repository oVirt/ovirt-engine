package org.ovirt.engine.core.bll;

import static java.util.Collections.sort;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.aaa.AuthzUtils;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetDomainListParameters;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.extensions.mgr.ExtensionsManager;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;

public class GetDomainListQuery<P extends GetDomainListParameters> extends QueriesCommandBase<P> {
    public GetDomainListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // Get the name of the internal domain:
        String internal = Config.<String> getValue(ConfigValues.AdminDomain);

        // Get the list of authentication profile names:
        List<ExtensionProxy> extensions =
                getExtensionsManager().getExtensionsByService(Authz.class.getName());
        List<String> names = new ArrayList<>(extensions.size());
        for (ExtensionProxy extension : extensions) {
            names.add(AuthzUtils.getName(extension));
        }
        if (getParameters().getFilterInternalDomain()) {
            names.remove(internal);
        }

        // Sort it so that the internal profile is always the last:
        sort(names, new LoginDomainComparator(internal));

        // Return the sorted list:
        getQueryReturnValue().setReturnValue(names);
    }

    protected ExtensionsManager getExtensionsManager() {
        return EngineExtensionsManager.getInstance();
    }
}
