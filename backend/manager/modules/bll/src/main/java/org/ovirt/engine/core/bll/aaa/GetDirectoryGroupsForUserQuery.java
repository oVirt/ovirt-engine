package org.ovirt.engine.core.bll.aaa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authz.PrincipalRecord;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetDirectoryGroupsForUserQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetDirectoryGroupsForUserQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDirectoryUser(getSessionDataContainer().getUser(getParameters().getSessionId(), false)));
    }

    private Collection<DirectoryGroup> getDirectoryUser(DbUser dbUser) {

        Collection<DirectoryGroup> groups = new ArrayList<>();

        Map<String, Object> response = SsoOAuthServiceUtils.findPrincipalsByIds(
                getSessionDataContainer().getSsoAccessToken(getParameters().getSessionId()),
                dbUser.getDomain(),
                dbUser.getNamespace(),
                Arrays.asList(dbUser.getExternalId()),
                true,
                true);

        Collection<ExtMap> principalRecords = Collections.emptyList();
        if (response.containsKey("result")) {
            principalRecords = (Collection<ExtMap>) response.get("result");
        }

        if (!principalRecords.isEmpty()) {
            ExtMap principalRecord = principalRecords.iterator().next();
            DirectoryUtils.flatGroups(principalRecord);
            for (ExtMap group : principalRecord.<Collection<ExtMap>>get(PrincipalRecord.GROUPS, Collections.<ExtMap> emptyList())) {
                groups.add(DirectoryUtils.mapGroupRecordToDirectoryGroup(dbUser.getDomain(), group));
            }
        }

        return groups;
    }
}
