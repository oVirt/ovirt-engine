package org.ovirt.engine.ui.uicommonweb.models.users;

@SuppressWarnings("unused")
public class UserGroup
{
    private String privateGroupName;

    public String getGroupName()
    {
        return privateGroupName;
    }

    public void setGroupName(String value)
    {
        privateGroupName = value;
    }

    private String privateOrganizationalUnit;

    public String getOrganizationalUnit()
    {
        return privateOrganizationalUnit;
    }

    public void setOrganizationalUnit(String value)
    {
        privateOrganizationalUnit = value;
    }

    private String privateDomain;

    public String getDomain()
    {
        return privateDomain;
    }

    public void setDomain(String value)
    {
        privateDomain = value;
    }
}
