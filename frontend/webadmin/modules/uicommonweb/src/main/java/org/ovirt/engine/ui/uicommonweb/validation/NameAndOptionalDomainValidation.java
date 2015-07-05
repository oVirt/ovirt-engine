package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NameAndOptionalDomainValidation extends BaseI18NValidation {

    @Override
    protected String composeRegex() {
        return "^[A-Za-z0-9_-]+(@[.A-Za-z0-9_-]+)?$"; //$NON-NLS-1$
    }

    @Override
    protected String composeMessage() {
        return ConstantsManager.getInstance().getConstants().asciiNameAndDomainValidationMsg();
    }

}
