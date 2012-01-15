package org.ovirt.engine.ui.uicommonweb.validation;

@SuppressWarnings("unused")
public class MacAddressValidation extends RegexValidation
{
    public MacAddressValidation()
    {
        setExpression("^([\\dabcdefABCDEF]{2}:?){6}$");
        setMessage("Invalid MAC address");
    }
}
