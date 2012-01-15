package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.RegexOptions;

@SuppressWarnings("unused")
public class AsciiOrNoneValidation implements IValidation
{

    public static final String ONLY_ASCII_OR_NONE = "[^\u0000-\u007F]";

    @Override
    public ValidationResult Validate(Object value)
    {
        ValidationResult result = new ValidationResult();
        // note: in backend java code the regex is [\\p{ASCII}]* which is not compatible with c#
        if (value != null && Regex.IsMatch(value.toString(), ONLY_ASCII_OR_NONE, RegexOptions.None))
        {
            result.setSuccess(false);
            result.setReasons(new java.util.ArrayList<String>(java.util.Arrays.asList(new String[] { "The field contains special characters. Only numbers, letters, '-' and '_' are allowed." })));
        }
        return result;
    }
}
