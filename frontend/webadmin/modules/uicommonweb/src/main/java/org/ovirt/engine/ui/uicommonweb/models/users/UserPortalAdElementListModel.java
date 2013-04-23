package org.ovirt.engine.ui.uicommonweb.models.users;

import java.util.ArrayList;
import java.util.Set;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class UserPortalAdElementListModel extends AdElementListModel {

    @Override
    protected void findUsers(String searchString, AsyncQuery query) {
        Frontend.RunQuery(VdcQueryType.GetAllDbUsers, getParameters(), query);
    }

    private VdcQueryParametersBase getParameters() {
        VdcQueryParametersBase parameters = new VdcQueryParametersBase();
        parameters.setFiltered(true);
        return parameters;
    }

    @Override
    protected void addUsersToModel(VdcQueryReturnValue returnValue, Set<Guid> excludeUsers) {
        Iterable<DbUser> filteredUsers = Linq.where((ArrayList<DbUser>) returnValue.getReturnValue(),
                new Linq.DbUserPredicate(getTargetDbUser()));
        for (DbUser dbUser : filteredUsers) {
            if (!excludeUsers.contains(dbUser.getId())) {
                EntityModel tempVar2 = new EntityModel();
                tempVar2.setEntity(dbUser);
                getusers().add(tempVar2);
            }
        }
    }

    private DbUser getTargetDbUser() {
        DbUser dbUser = new DbUser();
        dbUser.setLoginName(getSearchString());
        dbUser.setDomain(getDomain().getSelectedItem().toString());
        return dbUser;
    }

    @Override
    protected void findGroups(String searchString, AsyncQuery query) {
        AdElementListModel adElementListModel = (AdElementListModel) query.getModel();
        adElementListModel.setgroups(new ArrayList<EntityModel>());
        super.onUserAndAdGroupsLoaded(adElementListModel);
    }
}
