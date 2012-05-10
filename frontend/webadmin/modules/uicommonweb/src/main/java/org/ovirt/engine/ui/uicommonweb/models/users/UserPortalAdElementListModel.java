package org.ovirt.engine.ui.uicommonweb.models.users;

import org.ovirt.engine.core.common.queries.AdGroupsSearchParameters;
import org.ovirt.engine.core.common.queries.AdUsersSearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;

public class UserPortalAdElementListModel extends AdElementListModel {

    @Override
    protected void findUsers(String searchString, AsyncQuery query) {
        Frontend.RunQuery(VdcQueryType.AdUsersSearch, new AdUsersSearchParameters(searchString), query);
    }

    protected void findGroups(String searchString, AsyncQuery query) {
        Frontend.RunQuery(VdcQueryType.AdGroupsSearch, new AdGroupsSearchParameters(searchString), query);
    }
}
