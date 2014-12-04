package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authz.GroupRecord;
import org.ovirt.engine.api.extensions.aaa.Authz.PrincipalRecord;
import org.ovirt.engine.core.aaa.AuthzUtils;
import org.ovirt.engine.core.bll.aaa.DirectoryUtils;
import org.ovirt.engine.core.common.businessentities.aaa.AuthzGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;

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

        Collection<ExtMap> principalRecords = AuthzUtils.findPrincipalsByIds(
                EngineExtensionsManager.getInstance().getExtensionByName(dbUser.getDomain()),
                dbUser.getNamespace(),
                Arrays.asList(dbUser.getExternalId()),
                true,
                true);
        if (!principalRecords.isEmpty()) {
            ExtMap principalRecord = principalRecords.iterator().next();
            DirectoryUtils.flatGroups(principalRecord);
            for (ExtMap group : principalRecord.get(PrincipalRecord.GROUPS, Collections.<ExtMap> emptyList())) {
                groups.add(new AuthzGroup(dbUser.getDomain(), group.<String>get(GroupRecord.NAMESPACE), group.<String>get(GroupRecord.NAME)));
            }
        }

        return groups;
    }
}
