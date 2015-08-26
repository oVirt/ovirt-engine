package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIMessages;

public class KeyValueFormatValidation extends RegexValidation {

    private ConstantsManager constantsManager = ConstantsManager.getInstance();
    private final boolean emptyStringAllowed;

    public KeyValueFormatValidation() {
        this(false);
    }

    public KeyValueFormatValidation(boolean emptyStringAllowed) {
        this(ConstantsManager.getInstance(), emptyStringAllowed);
    }

    /***
     * For test, can't spy as trying to call
     * {@link org.ovirt.engine.ui.uicommonweb.validation.KeyValueFormatValidation#constantsManager} from constructor
     */
    KeyValueFormatValidation(ConstantsManager constantsManager) {
        this(constantsManager, false);
    }

    /***
     * For test, can't spy as trying to call
     * {@link org.ovirt.engine.ui.uicommonweb.validation.KeyValueFormatValidation#constantsManager} from constructor
     */
    KeyValueFormatValidation(ConstantsManager constantsManager, boolean emptyStringAllowed) {
        this.constantsManager = constantsManager;
        this.emptyStringAllowed = emptyStringAllowed;
        final String expression = calculateExpression();
        final String errorMessage = calculateErrorMessage();
        init(expression, errorMessage);
    }

    private String calculateExpression() {
        return emptyStringAllowed ?
                ValidationUtils.KEY_EQUALS_VALUE_SPACE_SEPARATED.concat("|").concat(ValidationUtils.EMPTY_STRING) : //$NON-NLS-1$
                ValidationUtils.KEY_EQUALS_VALUE_SPACE_SEPARATED;
    }

    private void init(String expression, String message) {
        setExpression(expression);
        setMessage(message);
    }

    private String calculateErrorMessage() {
        return emptyStringAllowed ? getMessages().emptyOrValidKeyValueFormatMessage(getKeyValueFormatMessage())
                : getMessages().customPropertiesValuesShouldBeInFormatReason(getKeyValueFormatMessage());
    }

    private String getKeyValueFormatMessage() {
        return getMessages().keyValueFormat();
    }

    private UIMessages getMessages() {
        return constantsManager.getMessages();
    }
}
