package org.ovirt.engine.ui.common.widget.uicommon.users;

import org.ovirt.engine.core.searchbackend.VdcUserConditionFieldAutoCompleter.UserOrGroup;

public interface UserTypeChangeHandler {
    /**
     * Called when the selected user type changes.
     * @param newType The new value of the user type.
     */
    void userTypeChanged(UserOrGroup newType);
}
