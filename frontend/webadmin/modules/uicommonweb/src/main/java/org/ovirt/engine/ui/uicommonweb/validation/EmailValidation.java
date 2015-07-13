package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.MailAddress;

@SuppressWarnings("unused")
public class EmailValidation implements IValidation {
    @Override
    public ValidationResult validate(Object value) {
        ValidationResult result = new ValidationResult();

        try {
            new MailAddress((String) value);
        } catch (RuntimeException e) {
            result.setSuccess(false);
            result.getReasons().add(ConstantsManager.getInstance().getConstants().invalidEmailAddressInvalidReason());
        }

        return result;
    }
}
