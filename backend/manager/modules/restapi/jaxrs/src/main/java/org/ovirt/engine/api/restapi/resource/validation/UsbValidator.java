package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.Usb;
import org.ovirt.engine.api.model.UsbType;

@ValidatedClass(clazz = Usb.class)
public class UsbValidator implements Validator<Usb> {

    @Override
    public void validateEnums(Usb usb) {
        if (usb != null) {
            if (usb.isSetType()) {
                validateEnum(UsbType.class, usb.getType(), true);
            }
        }
    }
}
