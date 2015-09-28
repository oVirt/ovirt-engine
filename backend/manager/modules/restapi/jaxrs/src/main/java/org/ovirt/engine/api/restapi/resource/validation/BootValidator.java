package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import java.util.List;

import org.ovirt.engine.api.model.Boot;
import org.ovirt.engine.api.model.BootDevice;

@ValidatedClass(clazz = Boot.class)
public class BootValidator implements Validator<Boot> {

    @Override
    public void validateEnums(Boot boot) {
        if (boot != null) {
            if (boot.isSetDevices() && boot.getDevices().isSetDevices()) {
                List<String> devices = boot.getDevices().getDevices();
                for (String device : devices) {
                    validateEnum(BootDevice.class, device, true);
                }
            }
        }
    }
}
