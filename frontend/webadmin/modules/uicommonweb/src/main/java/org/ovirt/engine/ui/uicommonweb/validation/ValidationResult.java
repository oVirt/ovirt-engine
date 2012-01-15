package org.ovirt.engine.ui.uicommonweb.validation;

@SuppressWarnings("unused")
public final class ValidationResult
{
    private boolean privateSuccess;

    public boolean getSuccess()
    {
        return privateSuccess;
    }

    public void setSuccess(boolean value)
    {
        privateSuccess = value;
    }

    private java.util.List<String> privateReasons;

    public java.util.List<String> getReasons()
    {
        return privateReasons;
    }

    public void setReasons(java.util.List<String> value)
    {
        privateReasons = value;
    }

    public ValidationResult()
    {
        setSuccess(true);
        setReasons(new java.util.ArrayList<String>());
    }
}
