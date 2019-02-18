package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NoTrimmingWhitespacesValidation extends RegexValidation {

    public NoTrimmingWhitespacesValidation() {
        setExpression("^$|^\\S.*\\S$|^\\S+$"); //$NON-NLS-1$
        setMessage(ConstantsManager.getInstance().getConstants().leadingOrTrailingSpacesInField());
    }

}
