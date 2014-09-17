package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.HostNetworkQosParametersModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
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

    private ListModel<VdsNetworkInterface> networkInterface;

    public ListModel<VdsNetworkInterface> getInterface()
    {
        return networkInterface;
    }

    private void setInterface(ListModel<VdsNetworkInterface> value)
    {
        networkInterface = value;
    }

    private EntityModel<String> address;

    public EntityModel<String> getAddress()
    {
        return address;
    }

    private void setAddress(EntityModel<String> value)
    {
        address = value;
    }

    private EntityModel<String> subnet;

    public EntityModel<String> getSubnet()
    {
        return subnet;
    }

    private void setSubnet(EntityModel<String> value)
    {
        subnet = value;
    }

    private EntityModel<String> gateway;

    public EntityModel<String> getGateway()
    {
        return gateway;
    }

    private void setGateway(EntityModel<String> value)
    {
        gateway = value;
    }

    private ListModel<Network> network;

    public ListModel<Network> getNetwork()
    {
        return network;
    }

    private void setNetwork(ListModel<Network> value)
    {
        network = value;
    }

    private EntityModel<Boolean> checkConnectivity;

    public EntityModel<Boolean> getCheckConnectivity()
    {
        return checkConnectivity;
    }

    private void setCheckConnectivity(EntityModel<Boolean> value)
    {
        checkConnectivity = value;
    }

    private ListModel<Map.Entry<String, EntityModel<String>>> bondingOptions;

    public ListModel<Map.Entry<String, EntityModel<String>>> getBondingOptions()
    {
        return bondingOptions;
    }

    private void setBondingOptions(ListModel<Map.Entry<String, EntityModel<String>>> value)
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

    private EntityModel<String> name;

    public EntityModel<String> getName()
    {
        return name;
    }

    public void setName(EntityModel<String> value)
    {
        name = value;
    }

    private EntityModel<Boolean> commitChanges;

    public EntityModel<Boolean> getCommitChanges()
    {
        return commitChanges;
    }

    public void setCommitChanges(EntityModel<Boolean> value)
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

    private EntityModel<Boolean> isToSync;

    public EntityModel<Boolean> getIsToSync() {
        return isToSync;
    }

    public void setIsToSync(EntityModel<Boolean> isToSync) {
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

    private HostNetworkQosParametersModel qosModel;

    public HostNetworkQosParametersModel getQosModel() {
        return qosModel;
    }

    private void setQosModel(HostNetworkQosParametersModel qosModel) {
        this.qosModel = qosModel;
    }

    private KeyValueModel customPropertiesModel;

    public KeyValueModel getCustomPropertiesModel() {
        return customPropertiesModel;
    }

    private void setCustomPropertiesModel(KeyValueModel customProperties) {
        this.customPropertiesModel = customProperties;
    }

    public HostInterfaceModel() {
        this(false);
    }

    public HostInterfaceModel(boolean compactMode)
    {
        setSetupNetworkMode(compactMode);
        setInterface(new ListModel<VdsNetworkInterface>());
        setNetwork(new ListModel<Network>());
        setName(new EntityModel<String>());
        setAddress(new EntityModel<String>());
        setSubnet(new EntityModel<String>());
        setGateway(new EntityModel<String>());
        setCheckConnectivity(new EntityModel<Boolean>());
        setBondingOptions(new ListModel<Map.Entry<String, EntityModel<String>>>());
        setCommitChanges(new EntityModel<Boolean>());
        setQosOverridden(new EntityModel<Boolean>());
        setQosModel(new HostNetworkQosParametersModel());
        setCustomPropertiesModel(new KeyValueModel());

        setIsToSync(new EntityModel<Boolean>(){
            @Override
            public void setEntity(Boolean value) {
                super.setEntity(value);
                if (getIsToSync().getIsChangable()){
                    if (!value){
                        revertChanges();
                    }
                    setBootProtocolsAvailable(value);
                    updateQosChangeability();
                    getCustomPropertiesModel().setIsChangable(value);
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
        getCustomPropertiesModel().setIsAvailable(false);

        getNetwork().getSelectedItemChangedEvent().addListener(this);
        getQosOverridden().getEntityChangedEvent().addListener(this);
    }

    private void revertChanges() {
        if (originalNetParams != null) {
            setBootProtocol(originalNetParams.getBootProtocol());
            getAddress().setEntity(originalNetParams.getAddress());
            getSubnet().setEntity(originalNetParams.getSubnet());
            getGateway().setEntity(originalNetParams.getGateway());
            getQosOverridden().setEntity(originalNetParams.getQosOverridden());
            getQosModel().init(originalNetParams.getQos());
            getCustomPropertiesModel().deserialize(KeyValueModel.convertProperties(originalNetParams.getCustomProperties()));
        }
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition) && sender == getNetwork()) {
            network_SelectedItemChanged(null);
        } else if (sender == getQosOverridden()) {
            updateQosChangeability();
        }
    }

    private void network_SelectedItemChanged(EventArgs e)
    {
        updateCanSpecify();

        Network network = getNetwork().getSelectedItem();
        setBootProtocolsAvailable((network != null && "None".equals(network.getName())) ? false //$NON-NLS-1$
                : true);

        if (getNetworks() != null)
        {
            for (VdsNetworkInterface item : getNetworks())
            {
                if (ObjectUtils.objectsEqual(item.getNetworkName(), network.getName()))
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

    private void updateQosChangeability() {
        getQosModel().setIsChangable(getQosOverridden().getEntity() && getQosOverridden().getIsChangable());
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
            getGateway().validateEntity(new IValidation[] { new IpAddressValidation(true) });
        }

        getQosModel().validate();
        getCustomPropertiesModel().validate();

        return getNetwork().getIsValid() && getAddress().getIsValid() && getSubnet().getIsValid()
                && getGateway().getIsValid() && getQosModel().getIsValid() && getCustomPropertiesModel().getIsValid();
    }
}
