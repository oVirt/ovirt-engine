package org.ovirt.engine.core.bll.aaa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.DirectoryIdQueryParameters;

public class GetDirectoryGroupByIdQuery<P extends DirectoryIdQueryParameters> extends QueriesCommandBase<P> {

    public GetDirectoryGroupByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Map<String, Object> response = findDirectoryGroupById();
        if (!response.containsKey("result")) {
            getQueryReturnValue().setSucceeded(false);
        } else {
            Collection<DirectoryGroup> groups = DirectoryUtils.mapGroupRecordsToDirectoryGroups(
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
