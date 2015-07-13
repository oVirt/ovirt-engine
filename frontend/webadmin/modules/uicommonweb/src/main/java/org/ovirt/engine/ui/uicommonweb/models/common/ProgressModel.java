package org.ovirt.engine.ui.uicommonweb.models.common;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class ProgressModel extends Model {

    private String currentOperation;

    public String getCurrentOperation() {
        return currentOperation;
    }

    public void setCurrentOperation(String value) {
        if (!ObjectUtils.objectsEqual(currentOperation, value)) {
            currentOperation = value;
            onPropertyChanged(new PropertyChangedEventArgs("CurrentOperation")); //$NON-NLS-1$
        }
    }

}
