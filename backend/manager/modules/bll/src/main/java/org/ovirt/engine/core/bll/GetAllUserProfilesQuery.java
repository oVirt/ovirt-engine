package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dao.UserProfileDao;

public class GetAllUserProfilesQuery <P extends QueryParametersBase>
        extends QueriesCommandBase<P> {

    @Inject
    private UserProfileDao userProfileDao;

    public GetAllUserProfilesQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
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
