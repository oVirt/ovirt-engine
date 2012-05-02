package org.ovirt.engine.ui.uicommonweb.validation;

import java.util.ArrayList;
import java.util.List;

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

    private List<String> privateReasons;

    public List<String> getReasons()
    {
        return privateReasons;
    }

    public void setReasons(List<String> value)
    {
        privateReasons = value;
    }

    public ValidationResult()
    {
        setSuccess(true);
        setReasons(new ArrayList<String>());
    }
}
