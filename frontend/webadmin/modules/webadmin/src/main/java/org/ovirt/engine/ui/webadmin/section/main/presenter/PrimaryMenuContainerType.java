package org.ovirt.engine.ui.webadmin.section.main.presenter;

public enum PrimaryMenuContainerType {
    COMPUTE("compute"), //$NON-NLS-1$
    NETWORK("network"), //$NON-NLS-1$
    STORAGE("storage"), //$NON-NLS-1$
    ADMINISTRATION("admin"); //$NON-NLS-1$

    private final String id;

    private PrimaryMenuContainerType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
