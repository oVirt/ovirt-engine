package org.ovirt.engine.api.restapi.resource.validation;

import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.model.Role;

@ValidatedClass(clazz = Role.class)
public class RoleValidator implements Validator<Role> {

    private PermitValidator permitValidator = new PermitValidator();

    @Override
    public void validateEnums(Role role) {
        if (role.isSetPermits()) {
            for (Permit permit : role.getPermits().getPermits()) {
                permitValidator.validateEnums(permit);
            }
        }
    }
}
