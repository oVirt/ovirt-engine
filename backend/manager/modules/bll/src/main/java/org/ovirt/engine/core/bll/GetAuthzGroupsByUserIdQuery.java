package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authz.GroupRecord;
import org.ovirt.engine.api.extensions.aaa.Authz.PrincipalRecord;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.bll.aaa.DirectoryUtils;
import org.ovirt.engine.core.common.businessentities.aaa.AuthzGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetAuthzGroupsByUserIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetAuthzGroupsByUserIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDirectoryUser(getDbFacade().getDbUserDao().get(getParameters().getId())));
    }

    private Collection<AuthzGroup> getDirectoryUser(DbUser dbUser) {

        Collection<AuthzGroup> groups = new ArrayList<>();
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
                groups.add(new AuthzGroup(dbUser.getDomain(), group.<String>get(GroupRecord.NAMESPACE), group.<String>get(GroupRecord.NAME)));
            }
        }

        return groups;
    }
}
