package org.ovirt.engine.ui.uicommonweb.models.common;

import java.util.Objects;

import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class ProgressModel extends Model {

    private String currentOperation;

    public String getCurrentOperation() {
        return currentOperation;
    }

    public void setCurrentOperation(String value) {
        if (!Objects.equals(currentOperation, value)) {
            currentOperation = value;
            onPropertyChanged(new PropertyChangedEventArgs("CurrentOperation")); //$NON-NLS-1$
        }
    }

}
