package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
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

public class NetworkAttachmentModel extends Model {

    private Network network;
    private VdsNetworkInterface nic;
    private NetworkAttachment networkAttachment;
    private HostNetworkQos networkQos;

    private EntityModel<String> address;
    private EntityModel<String> subnet;
    private EntityModel<String> gateway;
    private EntityModel<String> name;
    private NetworkBootProtocol bootProtocol = NetworkBootProtocol.values()[0];
    private boolean noneBootProtocolAvailable = true;
    private boolean bootProtocolsAvailable;
    private EntityModel<Boolean> isToSync;
    private HostNetworkQosParametersModel qosModel;
    private KeyValueModel customPropertiesModel;
    private boolean staticIpChangeAllowed = true;
    private EntityModel<Boolean> qosOverridden;

    public NetworkAttachmentModel(Network network,
            VdsNetworkInterface nic,
            NetworkAttachment networkAttachment,
            HostNetworkQos networkQos) {
        this.network = network;
        this.nic = nic;
        this.networkAttachment = networkAttachment;
        this.networkQos = networkQos;

        verifyInput(network, nic, networkAttachment);

        setName(new EntityModel<String>());
        setAddress(new EntityModel<String>());
        setSubnet(new EntityModel<String>());
        setGateway(new EntityModel<String>());
        setQosOverridden(new EntityModel<Boolean>());
        setQosModel(new HostNetworkQosParametersModel());
        setCustomPropertiesModel(new KeyValueModel());
        setIsToSync(new EntityModel<Boolean>());
        setBootProtocolsAvailable(true);
        getGateway().setIsAvailable(false);
        getAddress().setIsChangeable(false);
        getSubnet().setIsChangeable(false);
        getGateway().setIsChangeable(false);
        getQosOverridden().setIsAvailable(false);
        getQosModel().setIsAvailable(false);
        getCustomPropertiesModel().setIsAvailable(false);

        getQosOverridden().getEntityChangedEvent().addListener(this);
        getIsToSync().getEntityChangedEvent().addListener(this);
    }

    private void verifyInput(Network network, VdsNetworkInterface nic, NetworkAttachment networkAttachment) {
        boolean unmanaged = networkAttachment == null && nic != null;
        boolean newAttachment = networkAttachment != null && networkAttachment.getId() == null && network != null;
        boolean existingAttachment =
                networkAttachment != null && networkAttachment.getId() != null && network != null && nic != null;

        assert unmanaged || newAttachment || existingAttachment : "the input of the ctor is wrong"; //$NON-NLS-1$
    }

    public void syncWith(InterfacePropertiesAccessor interfacePropertiesAccessor) {
        setBootProtocol(interfacePropertiesAccessor.getBootProtocol());
        getAddress().setEntity(interfacePropertiesAccessor.getAddress());
        getSubnet().setEntity(interfacePropertiesAccessor.getNetmask());
        getGateway().setEntity(interfacePropertiesAccessor.getGateway());
        getQosModel().init(interfacePropertiesAccessor.getHostNetworkQos());
        getCustomPropertiesModel().deserialize(KeyValueModel.convertProperties(interfacePropertiesAccessor.getCustomProperties()));
    }

    public EntityModel<String> getAddress() {
        return address;
    }

    private void setAddress(EntityModel<String> value) {
        address = value;
    }

    public EntityModel<String> getSubnet() {
        return subnet;
    }

    private void setSubnet(EntityModel<String> value) {
        subnet = value;
    }

    public EntityModel<String> getGateway() {
        return gateway;
    }

    private void setGateway(EntityModel<String> value) {
        gateway = value;
    }

    public EntityModel<String> getName() {
        return name;
    }

    public void setName(EntityModel<String> value) {
        name = value;
    }

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

    public boolean getNoneBootProtocolAvailable() {
        return noneBootProtocolAvailable;
    }

    public void setNoneBootProtocolAvailable(boolean value) {
        if (noneBootProtocolAvailable != value) {
            noneBootProtocolAvailable = value;
            onPropertyChanged(new PropertyChangedEventArgs("NoneBootProtocolAvailable")); //$NON-NLS-1$
        }
    }

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

    public EntityModel<Boolean> getIsToSync() {
        return isToSync;
    }

    public void setIsToSync(EntityModel<Boolean> isToSync) {
        this.isToSync = isToSync;
    }

    public void setStaticIpChangeAllowed(boolean staticIpChangeAllowed) {
        this.staticIpChangeAllowed = staticIpChangeAllowed;
        updateCanSpecify();
    }

    public EntityModel<Boolean> getQosOverridden() {
        return qosOverridden;
    }

    public void setQosOverridden(EntityModel<Boolean> qosOverridden) {
        this.qosOverridden = qosOverridden;
    }

    public HostNetworkQosParametersModel getQosModel() {
        return qosModel;
    }

    private void setQosModel(HostNetworkQosParametersModel qosModel) {
        this.qosModel = qosModel;
    }

    public KeyValueModel getCustomPropertiesModel() {
        return customPropertiesModel;
    }

    private void setCustomPropertiesModel(KeyValueModel customProperties) {
        this.customPropertiesModel = customProperties;
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (sender == getQosOverridden()) {
            qosOverriddenChanged();
        }

        if (sender == getIsToSync()) {
            isToSyncChanged();
        }
    }

    private void qosOverriddenChanged() {
        if (getQosOverridden().getIsChangable()) {
            updateQosChangeability();
            if (!getQosOverridden().getEntity()) {
                getQosModel().init(networkQos);
            }
        }
    }

    private void updateQosChangeability() {
        getQosModel().setIsChangeable(getQosOverridden().getIsAvailable() && getQosOverridden().getIsChangable()
                && Boolean.TRUE.equals(getQosOverridden().getEntity()));
    }

    private void initValues() {
        boolean newAttachment = networkAttachment != null && networkAttachment.getId() == null;
        boolean syncedNetwork = networkAttachment != null && networkAttachment.getReportedConfigurations() != null
                && networkAttachment.getReportedConfigurations().isNetworkInSync();
        boolean shouldBeSyncedNetwork = !syncedNetwork && Boolean.TRUE.equals(getIsToSync().getEntity());
        if (newAttachment || syncedNetwork || shouldBeSyncedNetwork) {
            syncWith(new InterfacePropertiesAccessor.FromNetworkAttachment(networkAttachment, networkQos));
        } else {
            syncWith(new InterfacePropertiesAccessor.FromNic(nic));
        }
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

    private void isToSyncChanged() {
        initValues();

        Boolean isEditingEnabled = !getIsToSync().getIsChangable() || getIsToSync().getEntity();
        setBootProtocolsAvailable(isEditingEnabled);
        getQosOverridden().setIsChangeable(isEditingEnabled);
        updateQosChangeability();
        getCustomPropertiesModel().setIsChangeable(isEditingEnabled);
    }

    public Network getNetwork() {
        return network;
    }
}
