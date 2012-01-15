package org.ovirt.engine.ui.uicommonweb.validation;

@SuppressWarnings("unused")
public class ByteSizeValidation extends RegexValidation
{
    public ByteSizeValidation()
    {
        setExpression("^\\d+\\s*(m|mb|g|gb){0,1}\\s*$");
        setMessage("TODO:");
    }
}
