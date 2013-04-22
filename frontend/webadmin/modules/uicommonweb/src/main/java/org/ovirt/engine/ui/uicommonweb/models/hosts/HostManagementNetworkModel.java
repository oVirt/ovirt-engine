package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IpAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SubnetMaskValidation;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class HostManagementNetworkModel extends EntityModel
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

    @Override
    public Network getEntity()
    {
        return (Network) super.getEntity();
    }

    public void setEntity(Network value)
    {
        super.setEntity(value);
    }

    private ListModel privateInterface;

    public ListModel getInterface()
    {
        return privateInterface;
    }

    private void setInterface(ListModel value)
    {
        privateInterface = value;
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

    private EntityModel privateGateway;

    public EntityModel getGateway()
    {
        return privateGateway;
    }

    private void setGateway(EntityModel value)
    {
        privateGateway = value;
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

    private boolean bootProtocolsAvailable = true;

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
            onPropertyChanged(new PropertyChangedEventArgs("BootProtocolsAvailable")); //$NON-NLS-1$
        }
    }

    public boolean getIsStaticAddress()
    {
        return getBootProtocol() == NetworkBootProtocol.STATIC_IP;
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

    public HostManagementNetworkModel() {
        this(false);
    }

    public HostManagementNetworkModel(boolean compactMode)
    {
        setSetupNetworkMode(compactMode);
        setInterface(new ListModel());
        setAddress(new EntityModel());
        setSubnet(new EntityModel());
        setGateway(new EntityModel());

        setCheckConnectivity(new EntityModel());
        getCheckConnectivity().setEntity(true);
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

        EntityModel tempVar = new EntityModel();
        tempVar.setEntity(false);
        setCommitChanges(tempVar);
        UpdateFieldsByEntity();
    }

    private void revertChanges() {
        if (originalNetParams!=null){
            setBootProtocol(originalNetParams.getBootProtocol());
            getAddress().setEntity(originalNetParams.getAddress());
            getSubnet().setEntity(originalNetParams.getSubnet());
            getGateway().setEntity(originalNetParams.getGateway());
        }
    }

    private void UpdateFieldsByEntity()
    {
        UpdateCanSpecify();

        // ** TODO: When BootProtocol will be added to 'network', and when
        // ** BootProtocol, Address, Subnet, and Gateway will be added to
        // ** the Network Add/Edit dialog, the next lines will be uncommented.
        // ** DO NOT DELETE NEXT COMMENTED LINES!
        // var network = (network)Network;
        // BootProtocol = network == null ? null : network.bootProtocol;
        // Address.Value = network == null ? null : network.addr;
        // Subnet.Value = network == null ? null : network.subnet;
        // Gateway.Value = network == null ? null : network.gateway;
    }

    private void BootProtocolChanged()
    {
        UpdateCanSpecify();

        getAddress().setIsValid(true);
        getSubnet().setIsValid(true);
        getGateway().setIsValid(true);
    }

    private void UpdateCanSpecify()
    {
        boolean isChangable = bootProtocolsAvailable && getIsStaticAddress();
        getAddress().setIsChangable(isChangable);
        getSubnet().setIsChangable(isChangable);
        getGateway().setIsChangable(isChangable);
    }

    public boolean Validate()
    {
        getInterface().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        getAddress().setIsValid(true);
        getSubnet().setIsValid(true);

        if (getIsStaticAddress())
        {
            getAddress().validateEntity(new IValidation[] { new NotEmptyValidation(), new IpAddressValidation() });
            getSubnet().validateEntity(new IValidation[] { new NotEmptyValidation(), new SubnetMaskValidation() });
            getGateway().validateEntity(new IValidation[] { new IpAddressValidation() });
        }

        return getInterface().getIsValid() && getAddress().getIsValid() && getSubnet().getIsValid() && getGateway().getIsValid();
    }
}
