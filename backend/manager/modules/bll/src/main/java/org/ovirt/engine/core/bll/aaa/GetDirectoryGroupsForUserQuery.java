package org.ovirt.engine.core.bll.aaa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authz.PrincipalRecord;
import org.ovirt.engine.core.aaa.AuthzUtils;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;

public class GetDirectoryGroupsForUserQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetDirectoryGroupsForUserQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDirectoryUser(getDbFacade().getDbUserDao().get(getUserID())));
    }

    private Collection<DirectoryGroup> getDirectoryUser(DbUser dbUser) {

        Collection<DirectoryGroup> groups = new ArrayList<>();

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
                groups.add(DirectoryUtils.mapGroupRecordToDirectoryGroup(dbUser.getDomain(), group));
            }
        }

        return groups;
    }
}
