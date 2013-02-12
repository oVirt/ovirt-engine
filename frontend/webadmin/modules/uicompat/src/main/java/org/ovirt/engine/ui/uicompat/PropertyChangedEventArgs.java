package org.ovirt.engine.ui.uicompat;

public class PropertyChangedEventArgs extends EventArgs {

    public String PropertyName;

    public PropertyChangedEventArgs(String name) {
        this.PropertyName = name;
    }

}
