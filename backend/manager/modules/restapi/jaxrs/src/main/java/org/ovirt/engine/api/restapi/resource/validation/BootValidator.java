package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.Boot;
import org.ovirt.engine.api.model.BootDevice;

@ValidatedClass(clazz = Boot.class)
public class BootValidator implements Validator<Boot> {

    @Override
    public void validateEnums(Boot boot) {
        if (boot != null) {
            if (boot.isSetDev()) {
                validateEnum(BootDevice.class, boot.getDev(), true);
            }
        }
    }
}
