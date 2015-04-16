package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicommonweb.models.vms.IconWithOsDefault;

public class IconWithOsDefaultValidation implements IValidation {

    @Override
    public ValidationResult validate(Object value) {
        if (value instanceof IconWithOsDefault) {
            final IconWithOsDefault iconWithOsDefault = (IconWithOsDefault) value;
            return validate(iconWithOsDefault);
        }
        throw new IllegalArgumentException(
                "Illegal argument type: " + (value == null ? "null" : value.getClass().toString())); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private ValidationResult validate(IconWithOsDefault iconWithOsDefault) {
        return new IconValidation().validate(iconWithOsDefault.getIcon());
    }
}
