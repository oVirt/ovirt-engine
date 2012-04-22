package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class IpAddressValidation extends RegexValidation
{
    public IpAddressValidation()
    {
        setExpression("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"); //$NON-NLS-1$
        setMessage(ConstantsManager.getInstance().getConstants().thisFieldMustContainIPaddressInFormatMsg());
    }
}
