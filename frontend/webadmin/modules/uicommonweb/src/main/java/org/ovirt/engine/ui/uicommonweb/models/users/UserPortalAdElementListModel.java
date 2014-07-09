package org.ovirt.engine.ui.uicommonweb.models.users;

import java.util.ArrayList;
import java.util.Set;

import org.ovirt.engine.core.aaa.ProfileEntry;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class UserPortalAdElementListModel extends AdElementListModel {

    @Override
    protected void findUsers(String searchString, AsyncQuery query) {
        Frontend.getInstance().runQuery(VdcQueryType.GetAllDbUsers, getParameters(), query);
    }

    private VdcQueryParametersBase getParameters() {
        VdcQueryParametersBase parameters = new VdcQueryParametersBase();
        parameters.setFiltered(true);
        return parameters;
    }

    @Override
    protected void addUsersToModel(VdcQueryReturnValue returnValue, Set<String> excludeUsers) {
        Iterable<DbUser> filteredUsers = Linq.where((ArrayList<DbUser>) returnValue.getReturnValue(),
                new Linq.DbUserPredicate(getTargetDbUser()));
        for (DbUser dbUser : filteredUsers) {
            if (!excludeUsers.contains(dbUser.getExternalId())) {
                EntityModel tempVar2 = new EntityModel();
                tempVar2.setEntity(dbUser);
                getusers().add(tempVar2);
            }
        }
    }

    private DbUser getTargetDbUser() {
        DbUser dbUser = new DbUser();
        dbUser.setLoginName(getSearchString());
        dbUser.setDomain(((ProfileEntry) getProfile().getSelectedItem()).getAuthz());
        return dbUser;
    }

    @Override
    protected void addGroupsToModel(VdcQueryReturnValue returnValue, Set<String> excludeUsers) {
        Iterable<DbGroup> filteredGroups = Linq.where((ArrayList<DbGroup>) returnValue.getReturnValue(),
                new Linq.DbGroupPredicate(getTargetDbGroup()));

        for (DbGroup group : filteredGroups)
        {
            if (!excludeUsers.contains(group.getId().toString()))
            {
                DbUser dbUser = new DbUser();
                dbUser.setExternalId(group.getExternalId());
                dbUser.setFirstName(group.getName());
                dbUser.setLastName(""); //$NON-NLS-1$
                dbUser.setLoginName(""); //$NON-NLS-1$
                dbUser.setDomain(group.getDomain());
                dbUser.setNamespace(group.getNamespace());

                EntityModel entity = new EntityModel();
                entity.setEntity(dbUser);
                getgroups().add(entity);
            }
        }
    }

    private DbGroup getTargetDbGroup() {
        DbGroup dbGroup = new DbGroup();
        dbGroup.setName(getSearchString());
        dbGroup.setDomain(((ProfileEntry) getProfile().getSelectedItem()).getAuthz());
        return dbGroup;
    }

    @Override
    protected void findGroups(String searchString, AsyncQuery query) {
        Frontend.getInstance().runQuery(VdcQueryType.GetAllDbGroups, getParameters(), query);
    }
}
