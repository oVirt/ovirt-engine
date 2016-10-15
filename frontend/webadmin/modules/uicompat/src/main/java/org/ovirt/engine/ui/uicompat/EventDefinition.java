package org.ovirt.engine.ui.uicompat;

public class EventDefinition {
    private Class privateOwnerType;
    public Class getOwnerType() {
        return privateOwnerType;
    }
    private void setOwnerType(Class value) {
        privateOwnerType = value;
    }
    private String privateName;
    public String getName() {
        return privateName;
    }
    private void setName(String value) {
        privateName = value;
    }

    public EventDefinition(String name, Class ownerType) {
        setName(name);
        setOwnerType(ownerType);
    }
}
