package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.aaa.AuthzUtils;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;

public class GetAvailableNamespacesQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetAvailableNamespacesQuery(P parameters) {
        this(parameters, null);
    }

    public GetAvailableNamespacesQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }


    @Override
    protected void executeQueryCommand() {
        HashMap<String, List<String>> namespacesMap = new HashMap<>();
        for (ExtensionProxy authz: EngineExtensionsManager.getInstance().getExtensionsByService(Authz.class.getName())) {
            for (String namespace : authz.getContext().get(Authz.ContextKeys.AVAILABLE_NAMESPACES, Arrays.asList("*"))) {
                MultiValueMapUtils.addToMap(AuthzUtils.getName(authz), namespace, namespacesMap);

            }
        }
        for (List<String> entry : namespacesMap.values()) {
            Collections.sort(entry);
        }
        setReturnValue(namespacesMap);
    }
}
