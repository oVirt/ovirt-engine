package org.ovirt.engine.ui.uicommonweb.validation;

@SuppressWarnings("unused")
public class NoSpacesValidation extends RegexValidation
{
    public NoSpacesValidation()
    {
        setExpression("^[^\\s]+$");
        setMessage("This field can't contain spaces.");
    }
}
