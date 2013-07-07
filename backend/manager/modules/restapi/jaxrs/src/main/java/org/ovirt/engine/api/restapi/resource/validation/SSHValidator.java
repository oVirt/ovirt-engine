package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.SSH;
import org.ovirt.engine.api.restapi.model.AuthenticationMethod;

@ValidatedClass(clazz = SSH.class)
public class SSHValidator implements Validator<SSH> {
    @Override
    public void validateEnums(SSH ssh) {
        if (ssh.isSetAuthenticationMethod()) {
            validateEnum(AuthenticationMethod.class, ssh.getAuthenticationMethod(), true);
        }
    }
}
