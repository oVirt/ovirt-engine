package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class NotEmptyQuotaValidation implements IValidation {
    @Override
    public ValidationResult validate(Object value) {
        ValidationResult result = new ValidationResult();

        if (value == null
                || (value instanceof Quota
                && (((Quota)value).getId() == null || Guid.Empty.equals(((Quota)value).getId())))) {
            result.setSuccess(false);
            result.getReasons().add(ConstantsManager.getInstance().getConstants().thisFieldCantBeEmptyInvalidReason());
        }

        return result;
    }
}
