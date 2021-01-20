package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.validator.UserProfileValidator;
import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.queries.UserProfilePropertyIdQueryParameters;
import org.ovirt.engine.core.dao.UserProfileDao;

public class GetUserProfilePropertyQuery<P extends UserProfilePropertyIdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private UserProfileDao userProfileDao;

    private final UserProfileValidator validator = new UserProfileValidator();

    public GetUserProfilePropertyQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    private boolean validate(UserProfileProperty prop) {
        return validate(validator.authorized(getUser(), prop.getUserId()));
    }

    @Override
    protected void executeQueryCommand() {
        UserProfileProperty prop = userProfileDao.get(getParameters().getId());
        if (prop == null || prop.getType() != getParameters().getType() || !validate(prop)) {
            return;
        }

        getQueryReturnValue().setReturnValue(prop);
    }
}
