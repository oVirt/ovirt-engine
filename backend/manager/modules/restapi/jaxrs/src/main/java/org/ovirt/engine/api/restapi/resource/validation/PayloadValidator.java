package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.Payload;
import org.ovirt.engine.api.model.VmDeviceType;

@ValidatedClass(clazz = Payload.class)
public class PayloadValidator implements Validator<Payload> {

    @Override
    public void validateEnums(Payload payload) {
        if (payload != null) {
            if (payload.getType() != null) {
                validateEnum(VmDeviceType.class, payload.getType(), true);
            }
        }
    }
}
