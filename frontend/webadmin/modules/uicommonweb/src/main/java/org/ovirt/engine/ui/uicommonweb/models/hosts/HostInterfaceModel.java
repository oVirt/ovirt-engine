package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IpAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SubnetMaskValidation;

@SuppressWarnings("unused")
public class HostInterfaceModel extends EntityModel
{

    private boolean setupNetworkMode;

    public boolean isSetupNetworkMode()
    {
        return setupNetworkMode;
    }

    private void setSetupNetworkMode(boolean value)
    {
        setupNetworkMode = value;
    }

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

    private ArrayList<VdsNetworkInterface> privateNetworks;

    public ArrayList<VdsNetworkInterface> getNetworks()
    {
        return privateNetworks;
    }

    public void setNetworks(ArrayList<VdsNetworkInterface> value)
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
            OnPropertyChanged(new PropertyChangedEventArgs("BootProtocol")); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("NoneBootProtocolAvailable")); //$NON-NLS-1$
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
            UpdateCanSpecify();
            OnPropertyChanged(new PropertyChangedEventArgs("BootProtocolsAvailable")); //$NON-NLS-1$
        }
    }

    public boolean getIsStaticAddress()
    {
        return getBootProtocol() == NetworkBootProtocol.STATIC_IP;
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
        OnPropertyChanged(new PropertyChangedEventArgs("BondingOptionsOverrideNotification")); //$NON-NLS-1$
    }

    private EntityModel isToSync;

    public EntityModel getIsToSync() {
        return isToSync;
    }

    public void setIsToSync(EntityModel isToSync) {
        this.isToSync = isToSync;
    }

    private NetworkParameters originalNetParams = null;

    public void setOriginalNetParams(NetworkParameters originalNetParams) {
        this.originalNetParams = originalNetParams;
    }

    public HostInterfaceModel() {
        this(false);
    }

    public HostInterfaceModel(boolean compactMode)
    {
        setSetupNetworkMode(compactMode);
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

        setIsToSync(new EntityModel(){
            @Override
            public void setEntity(Object value) {
                super.setEntity(value);
                if (getIsToSync().getIsChangable()){
                    if (!(Boolean)value){
                        revertChanges();
                    }
                    setBootProtocolsAvailable((Boolean) value);
                }
            }

        });

        // call the Network_ValueChanged method to set all
        // properties according to default value of Network:
        Network_SelectedItemChanged(null);
    }

    private void revertChanges() {
        if (originalNetParams!=null){
            setBootProtocol(originalNetParams.getBootProtocol());
            getAddress().setEntity(originalNetParams.getAddress());
            getSubnet().setEntity(originalNetParams.getSubnet());
        }
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

        Network network = (Network) getNetwork().getSelectedItem();
        setBootProtocolsAvailable((network != null && StringHelper.stringsEqual(network.getname(), "None")) ? false //$NON-NLS-1$
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
                            && item.getBootProtocol() == NetworkBootProtocol.NONE ? NetworkBootProtocol.DHCP
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
        Network network = (Network) getNetwork().getSelectedItem();
        boolean isChangable = bootProtocolsAvailable && getIsStaticAddress();
        getAddress().setIsChangable(isChangable);
        getSubnet().setIsChangable(isChangable);
    }

    public boolean Validate()
    {
        getNetwork().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        getAddress().setIsValid(true);
        getSubnet().setIsValid(true);

        Network net = (Network) getNetwork().getSelectedItem();
        if (getIsStaticAddress())
        {
            getAddress().ValidateEntity(new IValidation[] { new NotEmptyValidation(), new IpAddressValidation() });
            getSubnet().ValidateEntity(new IValidation[] { new NotEmptyValidation(), new SubnetMaskValidation() });
        }

        return getNetwork().getIsValid() && getAddress().getIsValid() && getSubnet().getIsValid();
    }
}
