package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

public class KeyValuePairValidation implements IValidation {
    private final boolean allowAlsoKey;

    public KeyValuePairValidation() {
        this(false);
    }

    // allows key without value, i.e. key,key=value,key,key</param>
    public KeyValuePairValidation(boolean allowAlsoKey) {
        this.allowAlsoKey = allowAlsoKey;
    }

    @Override
    public ValidationResult validate(Object value) {
        ValidationResult result = new ValidationResult();

        if (value != null && value instanceof String && !value.equals("")){  //$NON-NLS-1$
            String strValue = (String) value;

            if (strValue.endsWith(",")){  //$NON-NLS-1$
                result.setSuccess(false);
            }
            else {
                // Try parse value.
                for (String pair : strValue.split("[,]", -1)){  //$NON-NLS-1$
                    if (!result.getSuccess()) {
                        break;
                    }

                    String[] array = pair.split("[=]", -1); //$NON-NLS-1$

                    // if the split length is 2, its a 'key=value'
                    // if the split length is 1 (key), we accept only when we allow it (allowAlsoKey==true)
                    if (allowAlsoKey) {
                        if (array.length < 1 || array.length > 2) {
                            result.setSuccess(false);
                            break;
                        }
                    }
                    else {
                        if (array.length != 2) {
                            result.setSuccess(false);
                            break;
                        }
                    }

                    for (String t : array) {
                        if (StringUtils.isEmpty(t.trim())) {
                            result.setSuccess(false);
                            break;
                        }
                    }
                    if (result.getSuccess() && array.length == 2) {
                        //No problems so far, check if there are type issues.
                        validateKeyValuePair(array[0], array[1], result);
                    }
                }
            }
        }

        if (!result.getSuccess()) {
            if (!allowAlsoKey) {
                result.getReasons().add(ConstantsManager.getInstance()
                        .getConstants()
                        .valueDoesntNotMatchPatternKeyValueKeyValueInvalidReason());
            }
            else {
                result.getReasons().add(ConstantsManager.getInstance()
                        .getConstants()
                        .valueDoesntNotNatchPatternKeyValueKeyKeyValueInvalidReason());
            }
        }

        return result;
    }

    protected void validateKeyValuePair(String key, String value, ValidationResult result) {
        //No-op, sub classes can override for specific tests they need to perform.
    }
}
