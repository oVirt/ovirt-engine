package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
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

public class HostInterfaceModel extends EntityModel<Network> {

    private EntityModel<String> address;

    public EntityModel<String> getAddress() {
        return address;
    }

    private void setAddress(EntityModel<String> value) {
        address = value;
    }

    private EntityModel<String> subnet;

    public EntityModel<String> getSubnet() {
        return subnet;
    }

    private void setSubnet(EntityModel<String> value) {
        subnet = value;
    }

    private EntityModel<String> gateway;

    public EntityModel<String> getGateway() {
        return gateway;
    }

    private void setGateway(EntityModel<String> value) {
        gateway = value;
    }

    private EntityModel<String> name;

    public EntityModel<String> getName() {
        return name;
    }

    public void setName(EntityModel<String> value) {
        name = value;
    }

    private NetworkBootProtocol bootProtocol = NetworkBootProtocol.values()[0];

    public NetworkBootProtocol getBootProtocol() {
        return bootProtocol;
    }

    public void setBootProtocol(NetworkBootProtocol value) {
        if (bootProtocol != value) {
            bootProtocol = value;
            bootProtocolChanged();
            onPropertyChanged(new PropertyChangedEventArgs("BootProtocol")); //$NON-NLS-1$
        }
    }

    private boolean noneBootProtocolAvailable = true;

    public boolean getNoneBootProtocolAvailable() {
        return noneBootProtocolAvailable;
    }

    public void setNoneBootProtocolAvailable(boolean value) {
        if (noneBootProtocolAvailable != value) {
            noneBootProtocolAvailable = value;
            onPropertyChanged(new PropertyChangedEventArgs("NoneBootProtocolAvailable")); //$NON-NLS-1$
        }
    }

    private boolean bootProtocolsAvailable;

    public boolean getBootProtocolsAvailable() {
        return bootProtocolsAvailable;
    }

    public void setBootProtocolsAvailable(boolean value) {
        if (bootProtocolsAvailable != value) {
            bootProtocolsAvailable = value;
            updateCanSpecify();
            onPropertyChanged(new PropertyChangedEventArgs("BootProtocolsAvailable")); //$NON-NLS-1$
        }
    }

    public boolean getIsStaticAddress() {
        return getBootProtocol() == NetworkBootProtocol.STATIC_IP;
    }

    private boolean bondingOptionsOverrideNotification;

    public boolean getBondingOptionsOverrideNotification() {
        return bondingOptionsOverrideNotification;
    }

    public void setBondingOptionsOverrideNotification(boolean value) {
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
        setName(new EntityModel<String>());
        setAddress(new EntityModel<String>());
        setSubnet(new EntityModel<String>());
        setGateway(new EntityModel<String>());
        setQosOverridden(new EntityModel<Boolean>());
        setQosModel(new HostNetworkQosParametersModel());
        setCustomPropertiesModel(new KeyValueModel());

        setIsToSync(new EntityModel<Boolean>() {
            @Override
            public void setEntity(Boolean value) {
                super.setEntity(value);
                if (getIsToSync().getIsChangable()) {
                    if (!value) {
                        revertChanges();
                    }
                    setBootProtocolsAvailable(value);
                    getQosOverridden().setIsChangeable(value);
                    updateQosChangeability();
                    getCustomPropertiesModel().setIsChangeable(value);
                }
            }

        });

        setBootProtocolsAvailable(true);
        getGateway().setIsAvailable(false);
        getAddress().setIsChangeable(false);
        getSubnet().setIsChangeable(false);
        getGateway().setIsChangeable(false);
        getQosOverridden().setIsAvailable(false);
        getQosModel().setIsAvailable(false);
        getCustomPropertiesModel().setIsAvailable(false);

        getQosOverridden().getEntityChangedEvent().addListener(this);
    }

    private void revertChanges() {
        if (originalNetParams != null) {
            setBootProtocol(originalNetParams.getBootProtocol());
            getAddress().setEntity(originalNetParams.getAddress());
            getSubnet().setEntity(originalNetParams.getSubnet());
            getGateway().setEntity(originalNetParams.getGateway());
            getQosOverridden().setEntity(originalNetParams.isQosOverridden());
            getQosModel().init(originalNetParams.getQos());
            getCustomPropertiesModel().deserialize(KeyValueModel.convertProperties(originalNetParams.getCustomProperties()));
        }
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (sender == getQosOverridden()) {
            updateQosChangeability();
        }
    }

    private void updateQosChangeability() {
        getQosModel().setIsChangeable(getQosOverridden().getIsAvailable() && getQosOverridden().getIsChangable()
                && getQosOverridden().getEntity());
    }

    private void bootProtocolChanged() {
        updateCanSpecify();

        getAddress().setIsValid(true);
        getSubnet().setIsValid(true);
        getGateway().setIsValid(true);
    }

    private void updateCanSpecify() {
        boolean isChangable = bootProtocolsAvailable && getIsStaticAddress();
        getAddress().setChangeProhibitionReason(isChangable && !staticIpChangeAllowed
                ? ConstantsManager.getInstance().getConstants().staticIpAddressSameAsHostname() : null);
        getAddress().setIsChangeable(isChangable && staticIpChangeAllowed);
        getSubnet().setIsChangeable(isChangable);
        getGateway().setIsChangeable(isChangable);
    }

    public boolean validate() {
        getAddress().setIsValid(true);
        getSubnet().setIsValid(true);
        getGateway().setIsValid(true);

        if (getIsStaticAddress()) {
            getAddress().validateEntity(new IValidation[] { new NotEmptyValidation(), new IpAddressValidation() });
            getSubnet().validateEntity(new IValidation[] { new NotEmptyValidation(),
                    new SubnetMaskValidation(true) });
            getGateway().validateEntity(new IValidation[] { new IpAddressValidation(true) });
        }

        getQosModel().validate();
        getCustomPropertiesModel().validate();

        return getAddress().getIsValid() && getSubnet().getIsValid()
                && getGateway().getIsValid() && getQosModel().getIsValid() && getCustomPropertiesModel().getIsValid();
    }
}
