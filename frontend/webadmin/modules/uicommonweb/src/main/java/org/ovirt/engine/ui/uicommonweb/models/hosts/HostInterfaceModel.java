package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IpAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

@SuppressWarnings("unused")
public class HostInterfaceModel extends EntityModel
{

    private EntityModel privateAddress;

    public EntityModel getAddress()
    {
        return privateAddress;
    }

    private void setAddress(EntityModel value)
    {
        privateAddress = value;
    }

    private EntityModel privateSubnet;

    public EntityModel getSubnet()
    {
        return privateSubnet;
    }

    private void setSubnet(EntityModel value)
    {
        privateSubnet = value;
    }

    private ListModel privateNetwork;

    public ListModel getNetwork()
    {
        return privateNetwork;
    }

    private void setNetwork(ListModel value)
    {
        privateNetwork = value;
    }

    private EntityModel privateCheckConnectivity;

    public EntityModel getCheckConnectivity()
    {
        return privateCheckConnectivity;
    }

    private void setCheckConnectivity(EntityModel value)
    {
        privateCheckConnectivity = value;
    }

    private ListModel privateBondingOptions;

    public ListModel getBondingOptions()
    {
        return privateBondingOptions;
    }

    private void setBondingOptions(ListModel value)
    {
        privateBondingOptions = value;
    }

    private java.util.ArrayList<VdsNetworkInterface> privateNetworks;

    public java.util.ArrayList<VdsNetworkInterface> getNetworks()
    {
        return privateNetworks;
    }

    public void setNetworks(java.util.ArrayList<VdsNetworkInterface> value)
    {
        privateNetworks = value;
    }

    private EntityModel privateName;

    public EntityModel getName()
    {
        return privateName;
    }

    public void setName(EntityModel value)
    {
        privateName = value;
    }

    private EntityModel privateCommitChanges;

    public EntityModel getCommitChanges()
    {
        return privateCommitChanges;
    }

    public void setCommitChanges(EntityModel value)
    {
        privateCommitChanges = value;
    }

    private NetworkBootProtocol bootProtocol = NetworkBootProtocol.values()[0];

    public NetworkBootProtocol getBootProtocol()
    {
        return bootProtocol;
    }

    public void setBootProtocol(NetworkBootProtocol value)
    {
        if (bootProtocol != value)
        {
            bootProtocol = value;
            BootProtocolChanged();
            OnPropertyChanged(new PropertyChangedEventArgs("BootProtocol"));
        }
    }

    private boolean noneBootProtocolAvailable = true;

    public boolean getNoneBootProtocolAvailable()
    {
        return noneBootProtocolAvailable;
    }

    public void setNoneBootProtocolAvailable(boolean value)
    {
        if (noneBootProtocolAvailable != value)
        {
            noneBootProtocolAvailable = value;
            OnPropertyChanged(new PropertyChangedEventArgs("NoneBootProtocolAvailable"));
        }
    }

    private boolean bootProtocolsAvailable;

    public boolean getBootProtocolsAvailable()
    {
        return bootProtocolsAvailable;
    }

    public void setBootProtocolsAvailable(boolean value)
    {
        if (bootProtocolsAvailable != value)
        {
            bootProtocolsAvailable = value;
            OnPropertyChanged(new PropertyChangedEventArgs("BootProtocolsAvailable"));
        }
    }

    public boolean getIsStaticAddress()
    {
        return getBootProtocol() == NetworkBootProtocol.StaticIp;
    }

    private boolean privatebondingOptionsOverrideNotification;

    private boolean getbondingOptionsOverrideNotification()
    {
        return privatebondingOptionsOverrideNotification;
    }

    private void setbondingOptionsOverrideNotification(boolean value)
    {
        privatebondingOptionsOverrideNotification = value;
    }

    public boolean getBondingOptionsOverrideNotification()
    {
        return getbondingOptionsOverrideNotification();
    }

    public void setBondingOptionsOverrideNotification(boolean value)
    {
        setbondingOptionsOverrideNotification(value);
        OnPropertyChanged(new PropertyChangedEventArgs("BondingOptionsOverrideNotification"));
    }

    public HostInterfaceModel()
    {
        setAddress(new EntityModel());
        setSubnet(new EntityModel());
        setNetwork(new ListModel());
        getNetwork().getSelectedItemChangedEvent().addListener(this);
        setName(new EntityModel());
        EntityModel tempVar = new EntityModel();
        tempVar.setEntity(false);
        setCommitChanges(tempVar);

        setCheckConnectivity(new EntityModel());
        getCheckConnectivity().setEntity(false);
        setBondingOptions(new ListModel());
        // call the Network_ValueChanged method to set all
        // properties according to default value of Network:
        Network_SelectedItemChanged(null);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.equals(ListModel.SelectedItemChangedEventDefinition) && sender == getNetwork())
        {
            Network_SelectedItemChanged(null);
        }
    }

    private void Network_SelectedItemChanged(EventArgs e)
    {
        UpdateCanSpecify();

        network network = (network) getNetwork().getSelectedItem();
        setBootProtocolsAvailable((network != null && StringHelper.stringsEqual(network.getname(), "None")) ? false
                : true);

        if (getNetworks() != null)
        {
            for (VdsNetworkInterface item : getNetworks())
            {
                if (StringHelper.stringsEqual(item.getNetworkName(), network.getname()))
                {
                    getAddress().setEntity(StringHelper.isNullOrEmpty(item.getAddress()) ? null : item.getAddress());
                    getSubnet().setEntity(StringHelper.isNullOrEmpty(item.getSubnet()) ? null : item.getSubnet());
                    setBootProtocol(!getNoneBootProtocolAvailable()
                            && item.getBootProtocol() == NetworkBootProtocol.None ? NetworkBootProtocol.Dhcp
                            : item.getBootProtocol());
                    break;
                }
            }
        }

    }

    private void BootProtocolChanged()
    {
        UpdateCanSpecify();

        getAddress().setIsValid(true);
        getSubnet().setIsValid(true);
    }

    private void UpdateCanSpecify()
    {
        network network = (network) getNetwork().getSelectedItem();
        boolean isChangable = getIsStaticAddress() && network != null && !network.getId().equals(NGuid.Empty);
        getAddress().setIsChangable(isChangable);
        getSubnet().setIsChangable(isChangable);
    }

    public boolean Validate()
    {
        getNetwork().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        getAddress().setIsValid(true);
        getSubnet().setIsValid(true);

        network net = (network) getNetwork().getSelectedItem();
        if (getIsStaticAddress() && getNetwork().getSelectedItem() != null && !net.getId().equals(NGuid.Empty))
        {
            getAddress().ValidateEntity(new IValidation[] { new NotEmptyValidation(), new IpAddressValidation() });
            getSubnet().ValidateEntity(new IValidation[] { new NotEmptyValidation(), new IpAddressValidation() });
        }

        return getNetwork().getIsValid() && getAddress().getIsValid() && getSubnet().getIsValid();
    }
}
