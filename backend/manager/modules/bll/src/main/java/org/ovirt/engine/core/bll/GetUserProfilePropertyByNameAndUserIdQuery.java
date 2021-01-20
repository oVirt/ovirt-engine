package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.validator.UserProfileValidator;
import org.ovirt.engine.core.common.queries.IdAndNameQueryParameters;
import org.ovirt.engine.core.dao.UserProfileDao;

public class GetUserProfilePropertyByNameAndUserIdQuery<P extends IdAndNameQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private UserProfileDao userProfileDao;

    private final UserProfileValidator validator = new UserProfileValidator();

    public GetUserProfilePropertyByNameAndUserIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    private boolean validate() {
        return validate(validator.authorized(getUser(), getParameters().getId()));
    }

    @Override
    protected void executeQueryCommand() {
        if (!validate()) {
            return;
        }

        getQueryReturnValue().setReturnValue(userProfileDao.getByName(getParameters().getName(), getParameters().getId()));
    }
}

