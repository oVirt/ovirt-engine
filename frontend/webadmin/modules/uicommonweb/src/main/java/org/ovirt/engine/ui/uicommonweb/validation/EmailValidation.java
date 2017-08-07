package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.RegexOptions;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class EmailValidation implements IValidation {

    private static String ATOM = "[^\\x00-\\x1F^\\(^\\)^\\<^\\>^\\@^\\,^\\;^\\:^\\\\^\\\"^\\.^\\[^\\]^\\s]"; //$NON-NLS-1$
    private static String DOMAIN = "(" + ATOM + "+(\\." + ATOM + "+)*"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    private static String IP_DOMAIN = "\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\]"; //$NON-NLS-1$

    private static String pattern = "^" + ATOM + "+(\\." + ATOM + "+)*@" + DOMAIN + "|" + IP_DOMAIN + ")$"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    @Override
    public ValidationResult validate(Object value) {
        if (!Regex.isMatch((String) value, pattern, RegexOptions.IgnoreCase)) {
            ValidationResult result = new ValidationResult();
            result.setSuccess(false);
            result.getReasons().add(ConstantsManager.getInstance().getConstants().invalidEmailAddressInvalidReason());
            return result;
        }
        return ValidationResult.ok();
    }
}
