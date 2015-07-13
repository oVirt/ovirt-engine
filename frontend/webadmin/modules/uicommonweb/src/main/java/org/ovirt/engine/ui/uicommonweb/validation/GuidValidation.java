package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class GuidValidation extends RegexValidation {
    public GuidValidation() {
        setExpression("^[0-9a-fA-F]{8,8}-[0-9a-fA-F]{4,4}-[0-9a-fA-F]{4,4}-[0-9a-fA-F]{4,4}-[0-9a-fA-F]{12,12}$"); //$NON-NLS-1$
        setMessage(ConstantsManager.getInstance().getConstants().invalidGuidMsg());
    }
}
