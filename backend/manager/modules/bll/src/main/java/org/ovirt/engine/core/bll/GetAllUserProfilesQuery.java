package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.UserProfileDao;

public class GetAllUserProfilesQuery <P extends VdcQueryParametersBase>
        extends QueriesCommandBase<P> {

    @Inject
    private UserProfileDao userProfileDao;

    public GetAllUserProfilesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        /*
           this query is meant to be invoked by ovirt-vmconsole helper(s).
           Since it fetches information from the backend without restrictions,
           we need to block it for non-internal calls.
         */
        if (isInternalExecution()) {
            getQueryReturnValue().setReturnValue(userProfileDao.getAll());
        }
    }
}
