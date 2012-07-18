package org.ovirt.engine.ui.uicommonweb.models.datacenters;



@SuppressWarnings("unused")
public class DataCenterNetworkModel extends NetworkModel
{
    private boolean privateIsNew;

    public boolean getIsNew()
    {
        return privateIsNew;
    }

    public void setIsNew(boolean value)
    {
        privateIsNew = value;
    }

    public DataCenterNetworkModel()
    {
    }

}
