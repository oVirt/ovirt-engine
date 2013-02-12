package org.ovirt.engine.ui.uicompat;

public class EventDefinition
{
    private java.lang.Class privateOwnerType;
    public java.lang.Class getOwnerType()
    {
        return privateOwnerType;
    }
    private void setOwnerType(java.lang.Class value)
    {
        privateOwnerType = value;
    }
    private String privateName;
    public String getName()
    {
        return privateName;
    }
    private void setName(String value)
    {
        privateName = value;
    }

    public EventDefinition(String name, java.lang.Class ownerType)
    {
        setName(name);
        setOwnerType(ownerType);
    }
}
