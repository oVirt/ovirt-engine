package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dao.UserProfileDao;

public class GetAllUserPublicSshKeysQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private UserProfileDao userProfileDao;

    public GetAllUserPublicSshKeysQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    private boolean validate() {
        return validate(
                ValidationResult
                        .failWith(EngineMessage.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION)
                        .when(!isInternalExecution())
        );
    }

    @Override
    protected void executeQueryCommand() {
        /*
           this query is meant to be invoked by ovirt-vmconsole helper(s).
           Since it fetches information from the backend without restrictions,
           we need to block it for non-internal calls.
         */
        if (!validate()) {
            return;
        }

        getQueryReturnValue().setReturnValue(userProfileDao.getAllPublicSshKeys());
    }
}
