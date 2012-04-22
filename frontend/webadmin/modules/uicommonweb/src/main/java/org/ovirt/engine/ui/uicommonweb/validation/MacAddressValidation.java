package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class MacAddressValidation extends RegexValidation
{
    public MacAddressValidation()
    {
        setExpression("^([\\dabcdefABCDEF]{2}:?){6}$"); //$NON-NLS-1$
        setMessage(ConstantsManager.getInstance().getConstants().invalidMacAddressMsg());
    }
}
