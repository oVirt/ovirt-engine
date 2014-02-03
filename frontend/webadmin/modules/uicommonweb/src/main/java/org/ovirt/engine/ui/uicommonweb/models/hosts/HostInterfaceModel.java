package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.BaseNetworkQosModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IpAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SubnetMaskValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

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

    private ListModel networkInterface;

    public ListModel getInterface()
    {
        return networkInterface;
    }

    private void setInterface(ListModel value)
    {
        networkInterface = value;
    }

    private EntityModel address;

    public EntityModel getAddress()
    {
        return address;
    }

    private void setAddress(EntityModel value)
    {
        address = value;
    }

    private EntityModel subnet;

    public EntityModel getSubnet()
    {
        return subnet;
    }

    private void setSubnet(EntityModel value)
    {
        subnet = value;
    }

    private EntityModel gateway;

    public EntityModel getGateway()
    {
        return gateway;
    }

    private void setGateway(EntityModel value)
    {
        gateway = value;
    }

    private ListModel network;

    public ListModel getNetwork()
    {
        return network;
    }

    private void setNetwork(ListModel value)
    {
        network = value;
    }

    private EntityModel checkConnectivity;

    public EntityModel getCheckConnectivity()
    {
        return checkConnectivity;
    }

    private void setCheckConnectivity(EntityModel value)
    {
        checkConnectivity = value;
    }

    private ListModel bondingOptions;

    public ListModel getBondingOptions()
    {
        return bondingOptions;
    }

    private void setBondingOptions(ListModel value)
    {
        bondingOptions = value;
    }

    private ArrayList<VdsNetworkInterface> networks;

    public ArrayList<VdsNetworkInterface> getNetworks()
    {
        return networks;
    }

    public void setNetworks(ArrayList<VdsNetworkInterface> value)
    {
        networks = value;
    }

    private EntityModel name;

    public EntityModel getName()
    {
        return name;
    }

    public void setName(EntityModel value)
    {
        name = value;
    }

    private EntityModel commitChanges;

    public EntityModel getCommitChanges()
    {
        return commitChanges;
    }

    public void setCommitChanges(EntityModel value)
    {
        commitChanges = value;
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
            bootProtocolChanged();
            onPropertyChanged(new PropertyChangedEventArgs("BootProtocol")); //$NON-NLS-1$
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
            onPropertyChanged(new PropertyChangedEventArgs("NoneBootProtocolAvailable")); //$NON-NLS-1$
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
            updateCanSpecify();
            onPropertyChanged(new PropertyChangedEventArgs("BootProtocolsAvailable")); //$NON-NLS-1$
        }
    }

    public boolean getIsStaticAddress()
    {
        return getBootProtocol() == NetworkBootProtocol.STATIC_IP;
    }

    private boolean bondingOptionsOverrideNotification;

    public boolean getBondingOptionsOverrideNotification()
    {
        return bondingOptionsOverrideNotification;
    }

    public void setBondingOptionsOverrideNotification(boolean value)
    {
        bondingOptionsOverrideNotification = value;
        onPropertyChanged(new PropertyChangedEventArgs("BondingOptionsOverrideNotification")); //$NON-NLS-1$
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

    private boolean staticIpChangeAllowed = true;

    public void setStaticIpChangeAllowed(boolean staticIpChangeAllowed) {
        this.staticIpChangeAllowed = staticIpChangeAllowed;
        updateCanSpecify();
    }

    private EntityModel<Boolean> qosOverridden;

    public EntityModel<Boolean> getQosOverridden() {
        return qosOverridden;
    }

    public void setQosOverridden(EntityModel<Boolean> qosOverridden) {
        this.qosOverridden = qosOverridden;
    }

    private BaseNetworkQosModel qosModel;

    public BaseNetworkQosModel getQosModel() {
        return qosModel;
    }

    private void setQosModel(BaseNetworkQosModel qosModel) {
        this.qosModel = qosModel;
    }

    public HostInterfaceModel() {
        this(false);
    }

    public HostInterfaceModel(boolean compactMode)
    {
        setSetupNetworkMode(compactMode);
        setInterface(new ListModel());
        setNetwork(new ListModel());
        setName(new EntityModel());
        setAddress(new EntityModel());
        setSubnet(new EntityModel());
        setGateway(new EntityModel());
        setCheckConnectivity(new EntityModel());
        setBondingOptions(new ListModel());
        setCommitChanges(new EntityModel());
        setQosOverridden(new EntityModel<Boolean>());
        setQosModel(new BaseNetworkQosModel());

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

        getCommitChanges().setEntity(false);
        getCheckConnectivity().setEntity(false);

        getInterface().setIsAvailable(false);
        setBootProtocolsAvailable(true);
        getGateway().setIsAvailable(false);
        getAddress().setIsChangable(false);
        getSubnet().setIsChangable(false);
        getGateway().setIsChangable(false);
        getQosOverridden().setIsAvailable(false);
        getQosModel().setIsAvailable(false);

        getNetwork().getSelectedItemChangedEvent().addListener(this);
        getQosOverridden().getEntityChangedEvent().addListener(this);
    }

    private void revertChanges() {
        if (originalNetParams != null) {
            setBootProtocol(originalNetParams.getBootProtocol());
            getAddress().setEntity(originalNetParams.getAddress());
            getSubnet().setEntity(originalNetParams.getSubnet());
            getGateway().setEntity(originalNetParams.getGateway());
        }
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition) && sender == getNetwork()) {
            network_SelectedItemChanged(null);
        } else if (sender == getQosOverridden()) {
            getQosModel().setIsChangable(getQosOverridden().getEntity());
        }
    }

    private void network_SelectedItemChanged(EventArgs e)
    {
        updateCanSpecify();

        Network network = (Network) getNetwork().getSelectedItem();
        setBootProtocolsAvailable((network != null && "None".equals(network.getName())) ? false //$NON-NLS-1$
                : true);

        if (getNetworks() != null)
        {
            for (VdsNetworkInterface item : getNetworks())
            {
                if (StringHelper.stringsEqual(item.getNetworkName(), network.getName()))
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

    private void bootProtocolChanged()
    {
        updateCanSpecify();

        getAddress().setIsValid(true);
        getSubnet().setIsValid(true);
        getGateway().setIsValid(true);
    }

    private void updateCanSpecify()
    {
        boolean isChangable = bootProtocolsAvailable && getIsStaticAddress();
        getAddress().setChangeProhibitionReason(isChangable && !staticIpChangeAllowed
                ? ConstantsManager.getInstance().getConstants().staticIpAddressSameAsHostname() : null);
        getAddress().setIsChangable(isChangable && staticIpChangeAllowed);
        getSubnet().setIsChangable(isChangable);
        getGateway().setIsChangable(isChangable);
    }

    public boolean validate()
    {
        getNetwork().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        getAddress().setIsValid(true);
        getSubnet().setIsValid(true);
        getGateway().setIsValid(true);

        if (getIsStaticAddress())
        {
            getAddress().validateEntity(new IValidation[] { new NotEmptyValidation(), new IpAddressValidation() });
            getSubnet().validateEntity(new IValidation[] { new NotEmptyValidation(), new SubnetMaskValidation() });
            if (getGateway().getEntity() != null) {
                getGateway().validateEntity(new IValidation[] { new IpAddressValidation() });
            }
        }

        getQosModel().validate();

        return getNetwork().getIsValid() && getAddress().getIsValid() && getSubnet().getIsValid()
                && getGateway().getIsValid() && getQosModel().getIsValid();
    }
}
