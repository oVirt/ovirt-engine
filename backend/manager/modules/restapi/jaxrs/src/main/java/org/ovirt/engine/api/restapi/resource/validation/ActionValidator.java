package org.ovirt.engine.api.restapi.resource.validation;

import org.ovirt.engine.api.model.Action;

@ValidatedClass(clazz = Action.class)
public class ActionValidator implements Validator<Action> {
    private SSHValidator sshValidator = new SSHValidator();

    @Override
    public void validateEnums(Action action) {
        if (action.isSetSsh()) {
            sshValidator.validateEnums(action.getSsh());
        }
    }
}
