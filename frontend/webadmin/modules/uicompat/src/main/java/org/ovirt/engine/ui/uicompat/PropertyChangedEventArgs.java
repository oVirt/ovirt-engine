package org.ovirt.engine.ui.uicompat;

public class PropertyChangedEventArgs extends EventArgs {
    public static final String PROGRESS = "Progress"; //$NON-NLS-1$

    public String propertyName;

    public PropertyChangedEventArgs(String name) {
        this.propertyName = name;
    }

}
