package org.ovirt.engine.core.bll;

import java.util.Collections;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dao.UserProfileDao;


public class GetUserProfileAsListQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private UserProfileDao userProfileDao;

    public GetUserProfileAsListQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        UserProfile userProfile = userProfileDao.getByUserId(getUserID());

        if (userProfile != null) {
            getQueryReturnValue().setReturnValue(Collections.singletonList(userProfile));
        } else {
            getQueryReturnValue().setReturnValue(Collections.emptyList());
        }
    }
}
