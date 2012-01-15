package org.ovirt.engine.ui.uicommonweb.validation;

@SuppressWarnings("unused")
public class IpAddressValidation extends RegexValidation
{
    public IpAddressValidation()
    {
        setExpression("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
        setMessage("This field must contain an IP address in format xxx.xxx.xxx.xxx");
    }
}
