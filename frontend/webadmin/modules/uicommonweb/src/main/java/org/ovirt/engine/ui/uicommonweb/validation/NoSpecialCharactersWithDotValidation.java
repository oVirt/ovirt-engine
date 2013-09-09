package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NoSpecialCharactersWithDotValidation extends RegexValidation {

    public NoSpecialCharactersWithDotValidation() {
        setExpression("[0-9a-zA-Z-_\\.]+"); //$NON-NLS-1$
        setMessage(ConstantsManager.getInstance().getConstants().noSpecialCharactersWithDotMsg());
    }

}
