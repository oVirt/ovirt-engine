package org.ovirt.engine.ui.uicommonweb.models.common;

import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.Model;

@SuppressWarnings("unused")
public class ProgressModel extends Model
{

    private String currentOperation;

    public String getCurrentOperation()
    {
        return currentOperation;
    }

    public void setCurrentOperation(String value)
    {
        if (!StringHelper.stringsEqual(currentOperation, value))
        {
            currentOperation = value;
            OnPropertyChanged(new PropertyChangedEventArgs("CurrentOperation"));
        }
    }

}
