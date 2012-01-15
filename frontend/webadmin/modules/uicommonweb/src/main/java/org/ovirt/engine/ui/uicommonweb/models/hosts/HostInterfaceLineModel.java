package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.Model;

@SuppressWarnings("unused")
public class HostInterfaceLineModel extends Model
{

    private VdsNetworkInterface privateInterface;

    public VdsNetworkInterface getInterface()
    {
        return privateInterface;
    }

    public void setInterface(VdsNetworkInterface value)
    {
        privateInterface = value;
    }

    private boolean isBonded;

    public boolean getIsBonded()
    {
        return isBonded;
    }

    public void setIsBonded(boolean value)
    {
        if (isBonded != value)
        {
            isBonded = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsBonded"));
        }
    }

    private String bondName;

    public String getBondName()
    {
        return bondName;
    }

    public void setBondName(String value)
    {
        if (!StringHelper.stringsEqual(bondName, value))
        {
            bondName = value;
            OnPropertyChanged(new PropertyChangedEventArgs("BondName"));
        }
    }

    private String address;

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String value)
    {
        if (!StringHelper.stringsEqual(address, value))
        {
            address = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Address"));
        }
    }

    private java.util.ArrayList<HostInterface> interfaces;

    public java.util.ArrayList<HostInterface> getInterfaces()
    {
        return interfaces;
    }

    public void setInterfaces(java.util.ArrayList<HostInterface> value)
    {
        if (interfaces != value)
        {
            interfaces = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Interfaces"));
        }
    }

    private java.util.ArrayList<HostVLan> vLans;

    public java.util.ArrayList<HostVLan> getVLans()
    {
        return vLans;
    }

    public void setVLans(java.util.ArrayList<HostVLan> value)
    {
        if (vLans != value)
        {
            vLans = value;
            OnPropertyChanged(new PropertyChangedEventArgs("VLans"));
        }
    }

    private String networkName;

    public String getNetworkName()
    {
        return networkName;
    }

    public void setNetworkName(String value)
    {
        if (!StringHelper.stringsEqual(networkName, value))
        {
            networkName = value;
            OnPropertyChanged(new PropertyChangedEventArgs("NetworkName"));
        }
    }

    private boolean isManagement;

    public boolean getIsManagement()
    {
        return isManagement;
    }

    public void setIsManagement(boolean value)
    {
        if (isManagement != value)
        {
            isManagement = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsManagement"));
        }
    }

}
