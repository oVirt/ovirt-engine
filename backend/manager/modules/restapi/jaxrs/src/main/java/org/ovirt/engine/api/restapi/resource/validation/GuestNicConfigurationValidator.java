package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.GuestNicConfiguration;

@ValidatedClass(clazz = GuestNicConfiguration.class)
public class GuestNicConfigurationValidator implements Validator<GuestNicConfiguration> {

    @Override
    public void validateEnums(GuestNicConfiguration entity) {
        if (entity != null) {
            validateEnum(BootProtocol.class, entity.getBootProtocol(), true);
        }
    }
}
