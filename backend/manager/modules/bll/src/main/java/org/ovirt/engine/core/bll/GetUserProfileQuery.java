package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.UserProfileDao;


public class GetUserProfileQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private UserProfileDao userProfileDao;

    public GetUserProfileQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(userProfileDao.getByUserId(getUserID()));
    }
}
