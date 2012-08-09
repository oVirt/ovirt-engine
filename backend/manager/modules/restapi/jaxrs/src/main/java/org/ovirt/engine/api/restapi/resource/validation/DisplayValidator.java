package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.DisplayType;

@ValidatedClass(clazz = Display.class)
public class DisplayValidator implements Validator<Display> {

    @Override
    public void validateEnums(Display display) {
        if (display != null) {
            if (display.isSetType()) {
                validateEnum(DisplayType.class, display.getType(), true);
            }
        }

    }

}
