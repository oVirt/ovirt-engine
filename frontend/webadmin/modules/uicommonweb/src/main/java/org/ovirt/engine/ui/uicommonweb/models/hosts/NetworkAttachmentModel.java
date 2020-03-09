package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasValidatedTabs;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.HostNetworkQosParametersModel;
import org.ovirt.engine.ui.uicommonweb.models.dnsconfiguration.DnsConfigurationModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.Ipv4AddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.Ipv6AddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SubnetMaskValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class NetworkAttachmentModel extends Model implements HasValidatedTabs {

    private Network network;
    private VdsNetworkInterface nic;
    private NetworkAttachment networkAttachment;
    private HostNetworkQos networkQos;
    private final DnsResolverConfiguration reportedDnsResolverConfiguration;

    private EntityModel<String> ipv4Address;
    private EntityModel<String> ipv4Subnet;
    private EntityModel<String> ipv4Gateway;
    private Ipv4BootProtocol ipv4BootProtocol = Ipv4BootProtocol.values()[0];

    private EntityModel<String> ipv6Address;
    private EntityModel<Integer> ipv6Prefix;
    private EntityModel<String> ipv6Gateway;
    private Ipv6BootProtocol ipv6BootProtocol = Ipv6BootProtocol.values()[0];

    private boolean bootProtocolsAvailable;
    private boolean ipv6AutoconfAvailable;
    private EntityModel<Boolean> isToSync;
    private HostNetworkQosParametersModel qosModel;
    private DnsConfigurationModel dnsConfigurationModel;
    private KeyValueModel customPropertiesModel;
    private EntityModel<Boolean> qosOverridden;

    public NetworkAttachmentModel(Network network,
            VdsNetworkInterface nic,
            NetworkAttachment networkAttachment,
            HostNetworkQos networkQos,
            DnsResolverConfiguration reportedDnsResolverConfiguration) {
        this.network = network;
        this.nic = nic;
        this.networkAttachment = networkAttachment;
        this.networkQos = networkQos;
        this.reportedDnsResolverConfiguration = reportedDnsResolverConfiguration;

        verifyInput(network, nic, networkAttachment);

        setIpv4Address(new EntityModel<String>());
        setIpv4Subnet(new EntityModel<String>());
        setIpv4Gateway(new EntityModel<String>());

        setIpv6Address(new EntityModel<String>());
        setIpv6Prefix(new EntityModel<Integer>());
        setIpv6Gateway(new EntityModel<String>());

        setQosOverridden(new EntityModel<Boolean>());
        setQosModel(new HostNetworkQosParametersModel());
        setDnsConfigurationModel(new DnsConfigurationModel());
        setCustomPropertiesModel(new KeyValueModel());
        setIsToSync(new EntityModel<Boolean>());
        setBootProtocolsAvailable(true);
        setIpv6AutoconfAvailable(true);

        getIpv4Gateway().setIsAvailable(true);
        getIpv4Address().setIsChangeable(false);
        getIpv4Subnet().setIsChangeable(false);
        getIpv4Gateway().setIsChangeable(false);

        getIpv6Gateway().setIsAvailable(true);
        getIpv6Address().setIsChangeable(false);
        getIpv6Prefix().setIsChangeable(false);
        getIpv6Gateway().setIsChangeable(false);

        getQosOverridden().setIsAvailable(false);
        getQosModel().setIsAvailable(false);
        getDnsConfigurationModel().setIsAvailable(false);
        getCustomPropertiesModel().setIsAvailable(false);

        getQosOverridden().getEntityChangedEvent().addListener(this);
        getIsToSync().getEntityChangedEvent().addListener(this);

        setTitle();
    }

    private void verifyInput(Network network, VdsNetworkInterface nic, NetworkAttachment networkAttachment) {
        boolean unmanaged = networkAttachment == null && nic != null;
        boolean newAttachment = networkAttachment != null && networkAttachment.getId() == null && network != null;
        boolean existingAttachment =
                networkAttachment != null && networkAttachment.getId() != null && network != null && nic != null;

        assert unmanaged || newAttachment || existingAttachment : "the input of the ctor is wrong"; //$NON-NLS-1$
    }

    public void syncWith(InterfacePropertiesAccessor interfacePropertiesAccessor) {
        setIpv4BootProtocol(interfacePropertiesAccessor.getIpv4BootProtocol());
        getIpv4Address().setEntity(interfacePropertiesAccessor.getIpv4Address());
        getIpv4Subnet().setEntity(interfacePropertiesAccessor.getIpv4Netmask());
        getIpv4Gateway().setEntity(interfacePropertiesAccessor.getIpv4Gateway());

        setIpv6BootProtocol(interfacePropertiesAccessor.getIpv6BootProtocol());
        getIpv6Address().setEntity(interfacePropertiesAccessor.getIpv6Address());
        getIpv6Prefix().setEntity(interfacePropertiesAccessor.getIpv6Prefix());
        getIpv6Gateway().setEntity(interfacePropertiesAccessor.getIpv6Gateway());

        getQosModel().init(interfacePropertiesAccessor.getHostNetworkQos());
        getCustomPropertiesModel().deserialize(KeyValueModel.convertProperties(interfacePropertiesAccessor.getCustomProperties()));
        getDnsConfigurationModel().setEntity(interfacePropertiesAccessor.getDnsResolverConfiguration());

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

    public EntityModel<String> getIpv6Address() {
        return ipv6Address;
    }

    private void setIpv6Address(EntityModel<String> value) {
        ipv6Address = value;
    }

    public EntityModel<Integer> getIpv6Prefix() {
        return ipv6Prefix;
    }

    private void setIpv6Prefix(EntityModel<Integer> value) {
        ipv6Prefix = value;
    }

    public EntityModel<String> getIpv6Gateway() {
        return ipv6Gateway;
    }

    private void setIpv6Gateway(EntityModel<String> value) {
        ipv6Gateway = value;
    }

    public Ipv4BootProtocol getIpv4BootProtocol() {
        return ipv4BootProtocol;
    }

    public void setIpv4BootProtocol(Ipv4BootProtocol value) {
        if (ipv4BootProtocol != value) {
            ipv4BootProtocol = value;
            ipv4BootProtocolChanged();
            onPropertyChanged(new PropertyChangedEventArgs("Ipv4BootProtocol")); //$NON-NLS-1$
        }
    }

    public Ipv6BootProtocol getIpv6BootProtocol() {
        return ipv6BootProtocol;
    }

    public void setIpv6BootProtocol(Ipv6BootProtocol value) {
        if (ipv6BootProtocol != value) {
            ipv6BootProtocol = value;
            ipv6BootProtocolChanged();
            onPropertyChanged(new PropertyChangedEventArgs("Ipv6BootProtocol")); //$NON-NLS-1$
        }
    }

    public boolean getBootProtocolsAvailable() {
        return bootProtocolsAvailable;
    }

    public void setBootProtocolsAvailable(boolean value) {
        if (bootProtocolsAvailable != value) {
            bootProtocolsAvailable = value;
            updateCanSpecifyIpv4();
            updateCanSpecifyIpv6();
            onPropertyChanged(new PropertyChangedEventArgs("BootProtocolsAvailable")); //$NON-NLS-1$
        }
    }

    public boolean getIsStaticIpv4Address() {
        return getIpv4BootProtocol() == Ipv4BootProtocol.STATIC_IP;
    }

    public boolean getIsStaticIpv6Address() {
        return getIpv6BootProtocol() == Ipv6BootProtocol.STATIC_IP;
    }

    public EntityModel<Boolean> getIsToSync() {
        return isToSync;
    }

    public void setIsToSync(EntityModel<Boolean> isToSync) {
        this.isToSync = isToSync;
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

    public DnsConfigurationModel getDnsConfigurationModel() {
        return dnsConfigurationModel;
    }

    public void setDnsConfigurationModel(DnsConfigurationModel dnsConfigurationModel) {
        this.dnsConfigurationModel = dnsConfigurationModel;
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
        if (networkAttachment == null) {
            return;
        }

        boolean newAttachment = networkAttachment.getId() == null;

        // If the 'ReportedConfigurations' of the attachment is 'null' it means the attachment wasn't reported from the
        // engine (all the attachments reported from the engine have
        // 'ReportedConfigurations'). So it means this network is for sure in sync, since the ui already modified it and
        // created new instance of the attachment for it (an out-of-sync network cannot be modified).
        boolean attachmentNotReportedByTheEngine = networkAttachment.getReportedConfigurations() == null;

        boolean syncedNetwork = attachmentNotReportedByTheEngine || networkAttachment.getReportedConfigurations().isNetworkInSync();
        boolean syncRequestedByUser = !syncedNetwork && Boolean.TRUE.equals(getIsToSync().getEntity());
        if (newAttachment || syncedNetwork || syncRequestedByUser) {
            syncWith(new InterfacePropertiesAccessor.FromNetworkAttachmentForModel(networkAttachment, networkQos, nic));
        } else {
            syncWith(new InterfacePropertiesAccessor.FromNic(nic, reportedDnsResolverConfiguration));
        }
    }

    private void ipv4BootProtocolChanged() {
        updateCanSpecifyIpv4();

        getIpv4Address().setIsValid(true);
        getIpv4Subnet().setIsValid(true);
        getIpv4Gateway().setIsValid(true);
    }

    private void ipv6BootProtocolChanged() {
        updateCanSpecifyIpv6();

        getIpv6Address().setIsValid(true);
        getIpv6Prefix().setIsValid(true);
        getIpv6Gateway().setIsValid(true);
    }

    private void updateCanSpecifyIpv4() {
        boolean isChangeable = bootProtocolsAvailable && getIsStaticIpv4Address();
        getIpv4Address().setIsChangeable(isChangeable);
        getIpv4Subnet().setIsChangeable(isChangeable);
        getIpv4Gateway().setIsChangeable(isChangeable);
    }

    private void updateCanSpecifyIpv6() {
        boolean isChangeable = bootProtocolsAvailable && getIsStaticIpv6Address();
        getIpv6Address().setIsChangeable(isChangeable);
        getIpv6Prefix().setIsChangeable(isChangeable);
        getIpv6Gateway().setIsChangeable(isChangeable && getNetwork().getCluster().isDefaultRoute());
    }

    public boolean validate() {
        getIpv4Address().setIsValid(true);
        getIpv4Subnet().setIsValid(true);
        getIpv4Gateway().setIsValid(true);

        if (getIsStaticIpv4Address()) {
            getIpv4Address().validateEntity(new IValidation[] {
                    new NotEmptyValidation(),
                    new Ipv4AddressValidation() });
            getIpv4Subnet().validateEntity(new IValidation[] {
                    new NotEmptyValidation(),
                    new SubnetMaskValidation(true) });
            getIpv4Gateway().validateEntity(new IValidation[] { new Ipv4AddressValidation(true) });
        }

        getIpv6Address().setIsValid(true);
        getIpv6Prefix().setIsValid(true);
        getIpv6Gateway().setIsValid(true);

        if (getIsStaticIpv6Address()) {
            getIpv6Address().validateEntity(new IValidation[] {
                    new NotEmptyValidation(),
                    new Ipv6AddressValidation() });
            getIpv6Prefix()
                    .validateEntity(new IValidation[] { new NotEmptyValidation(), new IntegerValidation(0, 128) });
            getIpv6Gateway().validateEntity(new IValidation[] { new Ipv6AddressValidation(true) });
        }

        getQosModel().validate();
        getDnsConfigurationModel().validate();
        getCustomPropertiesModel().validate();

        setValidTab(TabName.IPV4_TAB,
                getIpv4Address().getIsValid() && getIpv4Gateway().getIsValid() && getIpv4Subnet().getIsValid());
        setValidTab(TabName.IPV6_TAB,
                getIpv6Address().getIsValid() && getIpv6Gateway().getIsValid() && getIpv6Prefix().getIsValid());
        setValidTab(TabName.QOS_TAB, getQosModel().getIsValid());
        setValidTab(TabName.CUSTOM_PROPERTIES_TAB, getCustomPropertiesModel().getIsValid());
        setValidTab(TabName.DNS_CONFIGURATION_TAB, getDnsConfigurationModel().getIsValid());

        return allTabsValid();
    }

    private void isToSyncChanged() {
        // The widget has to be available in order to update its values
        setBootProtocolsAvailable(true);
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

    protected Model setTitle() {
        return setTitle(ConstantsManager.getInstance()
                .getMessages()
                .editNetworkTitle(network.getName()));
    }

    public boolean getIpv6AutoconfAvailable() {
        return ipv6AutoconfAvailable;
    }

    public void setIpv6AutoconfAvailable(boolean ipv6AutoconfAvailable) {
        this.ipv6AutoconfAvailable = ipv6AutoconfAvailable;
    }
}
