package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.Model;

@SuppressWarnings("unused")
public class HostVLan extends Model
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

    private String name;

    public String getName()
    {
        return name;
    }

    public void setName(String value)
    {
        if (!StringHelper.stringsEqual(name, value))
        {
            name = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Name"));
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

}
