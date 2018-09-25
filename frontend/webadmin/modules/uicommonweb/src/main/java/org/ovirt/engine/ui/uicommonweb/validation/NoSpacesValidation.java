package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class NoSpacesValidation extends RegexValidation {
    public NoSpacesValidation() {
        setExpression("^[^\\s]*$"); //$NON-NLS-1$
        setMessage(ConstantsManager.getInstance().getConstants().thisFieldCantConatainSpacesMsg());
    }
}
