package org.ovirt.engine.core.bll.aaa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.DirectoryIdQueryParameters;

public class GetDirectoryGroupByIdQuery<P extends DirectoryIdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private DirectoryUtils directoryUtils;

    public GetDirectoryGroupByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Map<String, Object> response = findDirectoryGroupById();
        if (!response.containsKey("result")) {
            getQueryReturnValue().setSucceeded(false);
        } else {
            Collection<DirectoryGroup> groups = directoryUtils.mapGroupRecordsToDirectoryGroups(
                    getParameters().getDomain(),
                    (Collection<ExtMap>) response.get("result"));
            if (!groups.isEmpty()) {
                getQueryReturnValue().setReturnValue(new ArrayList<>(groups).get(0));
            }
        }
    }

    private Map<String, Object> findDirectoryGroupById() {
        return SsoOAuthServiceUtils.findDirectoryGroupById(
                getSessionDataContainer().getSsoAccessToken(getParameters().getSessionId()),
                getParameters().getDomain(),
                getParameters().getNamespace(),
                getParameters().getId(),
                false,
                false);
    }

}
