package org.ovirt.engine.ui.uicompat;

public class PropertyChangedEventArgs extends EventArgs {

    public String propertyName;

    public PropertyChangedEventArgs(String name) {
        this.propertyName = name;
    }

}
