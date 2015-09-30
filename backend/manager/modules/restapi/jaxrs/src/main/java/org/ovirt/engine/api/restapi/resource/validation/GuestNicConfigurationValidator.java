package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.NicConfiguration;

@ValidatedClass(clazz = NicConfiguration.class)
public class GuestNicConfigurationValidator implements Validator<NicConfiguration> {

    @Override
    public void validateEnums(NicConfiguration entity) {
        if (entity != null) {
            validateEnum(BootProtocol.class, entity.getBootProtocol(), true);
        }
    }
}
