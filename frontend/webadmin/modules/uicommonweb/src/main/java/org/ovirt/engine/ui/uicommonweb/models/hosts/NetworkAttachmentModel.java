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

    private EntityModel<String> ipv4Address;
    private EntityModel<String> ipv4Subnet;
    private EntityModel<String> ipv4Gateway;
    private NetworkBootProtocol ipv4BootProtocol = NetworkBootProtocol.values()[0];

    private EntityModel<String> name;
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
        setIpv4Address(new EntityModel<String>());
        setIpv4Subnet(new EntityModel<String>());
        setIpv4Gateway(new EntityModel<String>());
        setQosOverridden(new EntityModel<Boolean>());
        setQosModel(new HostNetworkQosParametersModel());
        setCustomPropertiesModel(new KeyValueModel());
        setIsToSync(new EntityModel<Boolean>());
        setBootProtocolsAvailable(true);
        getIpv4Gateway().setIsAvailable(false);
        getIpv4Address().setIsChangeable(false);
        getIpv4Subnet().setIsChangeable(false);
        getIpv4Gateway().setIsChangeable(false);
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
        setIpv4BootProtocol(interfacePropertiesAccessor.getBootProtocol());
        getIpv4Address().setEntity(interfacePropertiesAccessor.getAddress());
        getIpv4Subnet().setEntity(interfacePropertiesAccessor.getNetmask());
        getIpv4Gateway().setEntity(interfacePropertiesAccessor.getGateway());
        getQosModel().init(interfacePropertiesAccessor.getHostNetworkQos());
        getCustomPropertiesModel().deserialize(KeyValueModel.convertProperties(interfacePropertiesAccessor.getCustomProperties()));
    }

    public EntityModel<String> getIpv4Address() {
        return ipv4Address;
    }

    private void setIpv4Address(EntityModel<String> value) {
        ipv4Address = value;
    }

    public EntityModel<String> getIpv4Subnet() {
        return ipv4Subnet;
    }

    private void setIpv4Subnet(EntityModel<String> value) {
        ipv4Subnet = value;
    }

    public EntityModel<String> getIpv4Gateway() {
        return ipv4Gateway;
    }

    private void setIpv4Gateway(EntityModel<String> value) {
        ipv4Gateway = value;
    }

    public EntityModel<String> getName() {
        return name;
    }

    public void setName(EntityModel<String> value) {
        name = value;
    }

    public NetworkBootProtocol getIpv4BootProtocol() {
        return ipv4BootProtocol;
    }

    public void setIpv4BootProtocol(NetworkBootProtocol value) {
        if (ipv4BootProtocol != value) {
            ipv4BootProtocol = value;
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
        return getIpv4BootProtocol() == NetworkBootProtocol.STATIC_IP;
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

        getIpv4Address().setIsValid(true);
        getIpv4Subnet().setIsValid(true);
        getIpv4Gateway().setIsValid(true);
    }

    private void updateCanSpecify() {
        boolean isChangable = bootProtocolsAvailable && getIsStaticAddress();
        getIpv4Address().setChangeProhibitionReason(isChangable && !staticIpChangeAllowed
                ? ConstantsManager.getInstance().getConstants().staticIpAddressSameAsHostname() : null);
        getIpv4Address().setIsChangeable(isChangable && staticIpChangeAllowed);
        getIpv4Subnet().setIsChangeable(isChangable);
        getIpv4Gateway().setIsChangeable(isChangable);
    }

    public boolean validate() {
        getIpv4Address().setIsValid(true);
        getIpv4Subnet().setIsValid(true);
        getIpv4Gateway().setIsValid(true);

        if (getIsStaticAddress()) {
            getIpv4Address().validateEntity(new IValidation[] { new NotEmptyValidation(), new IpAddressValidation() });
            getIpv4Subnet().validateEntity(new IValidation[] { new NotEmptyValidation(),
                    new SubnetMaskValidation(true) });
            getIpv4Gateway().validateEntity(new IValidation[] { new IpAddressValidation(true) });
        }

        getQosModel().validate();
        getCustomPropertiesModel().validate();

        return getIpv4Address().getIsValid() && getIpv4Subnet().getIsValid() && getIpv4Gateway().getIsValid()
                && getQosModel().getIsValid() && getCustomPropertiesModel().getIsValid();
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
