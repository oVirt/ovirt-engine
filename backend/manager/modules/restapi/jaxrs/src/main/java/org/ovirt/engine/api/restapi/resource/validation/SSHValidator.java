package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.Ssh;
import org.ovirt.engine.api.restapi.model.AuthenticationMethod;

@ValidatedClass(clazz = Ssh.class)
public class SSHValidator implements Validator<Ssh> {
    @Override
    public void validateEnums(Ssh ssh) {
        if (ssh.isSetAuthenticationMethod()) {
            validateEnum(AuthenticationMethod.class, ssh.getAuthenticationMethod(), true);
        }
    }
}
