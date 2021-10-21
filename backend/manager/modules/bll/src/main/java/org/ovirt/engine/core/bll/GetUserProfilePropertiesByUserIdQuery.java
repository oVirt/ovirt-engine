package org.ovirt.engine.core.bll;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.validator.UserProfileValidator;
import org.ovirt.engine.core.common.queries.UserProfilePropertyIdQueryParameters;
import org.ovirt.engine.core.dao.UserProfileDao;

public class GetUserProfilePropertiesByUserIdQuery<P extends UserProfilePropertyIdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private UserProfileDao userProfileDao;

    private final UserProfileValidator validator = new UserProfileValidator();

    public GetUserProfilePropertiesByUserIdQuery(P parameters, EngineContext engineContext) {
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
        getQueryReturnValue().setReturnValue(userProfileDao.getAll(getParameters().getId())
                .stream()
                .filter(prop -> getParameters().getType() == null || prop.getType().equals(getParameters().getType()))
                .collect(Collectors.toList()));
    }
}
