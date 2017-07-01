package org.ovirt.engine.ui.uicommonweb.models.users;

import java.util.ArrayList;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class UserPortalAdElementListModel extends AdElementListModel {

    @Override
    protected void findUsers(String searchString, AsyncQuery query) {
        Frontend.getInstance().runQuery(QueryType.GetAllDbUsers, getParameters(), query);
    }

    private QueryParametersBase getParameters() {
        QueryParametersBase parameters = new QueryParametersBase();
        parameters.setFiltered(true);
        return parameters;
    }

    @Override
    protected void addUsersToModel(QueryReturnValue returnValue, Set<String> excludeUsers) {
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
        dbUser.setDomain(getProfile().getSelectedItem().getAuthz());
        return dbUser;
    }

    @Override
    protected void findGroups(String searchString, AsyncQuery query) {
        Frontend.getInstance().runQuery(QueryType.GetAllDbGroups, getParameters(), query);
    }
}
