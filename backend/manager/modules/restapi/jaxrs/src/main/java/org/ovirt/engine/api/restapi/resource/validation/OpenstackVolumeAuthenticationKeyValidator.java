package org.ovirt.engine.api.restapi.resource.validation;

import org.ovirt.engine.api.common.util.EnumValidator;
import org.ovirt.engine.api.model.OpenstackVolumeAuthenticationKey;
import org.ovirt.engine.api.model.OpenstackVolumeAuthenticationKeyUsageType;

@ValidatedClass(clazz = OpenstackVolumeAuthenticationKey.class)
public class OpenstackVolumeAuthenticationKeyValidator implements Validator<OpenstackVolumeAuthenticationKey> {

    @Override
    public void validateEnums(OpenstackVolumeAuthenticationKey entity) {
        if (entity != null) {
            EnumValidator.validateEnum(OpenstackVolumeAuthenticationKeyUsageType.class, entity.getUsageType(), true);
        }
    }

}
