package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.RngDevice;
import org.ovirt.engine.api.model.RngSource;

@ValidatedClass(clazz = RngDevice.class)
public class RngDeviceValidator implements Validator<RngDevice> {

    @Override
    public void validateEnums(RngDevice entity) {
        if (entity != null && entity.isSetSource()) {
            validateEnum(RngSource.class, entity.getSource(), true);
        }
    }

}
