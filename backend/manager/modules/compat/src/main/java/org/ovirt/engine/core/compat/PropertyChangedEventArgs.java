package org.ovirt.engine.core.compat;

public class PropertyChangedEventArgs extends EventArgs {

    public String PropertyName;

    public PropertyChangedEventArgs(String name) {
        // Incorrect: The 'property changed' classes should be removed as they
        // are not used for server side
        // Correct: they may be needed now that GWT compiled the VdcCommon to
        // javascript to update the gui...
        this.PropertyName = name;
    }

}
