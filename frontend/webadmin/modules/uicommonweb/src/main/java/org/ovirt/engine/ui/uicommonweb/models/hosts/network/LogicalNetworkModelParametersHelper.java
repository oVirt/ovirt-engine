package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import org.ovirt.engine.core.common.businessentities.network.AnonymousHostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.ui.uicommonweb.models.hosts.InterfacePropertiesAccessor;
import org.ovirt.engine.ui.uicommonweb.models.hosts.NetworkParameters;

public class LogicalNetworkModelParametersHelper {

    private LogicalNetworkModel networkModel;

    public LogicalNetworkModelParametersHelper(LogicalNetworkModel networkModel) {
        this.networkModel = networkModel;
    }

    public void prepareSetupNetworksParamsToAttachTo(NetworkInterfaceModel targetNic) {
        NetworkAttachment networkAttachment = createAttachmentWhenAttachingTo(targetNic.getOriginalIface());

        networkModel.getSetupModel()
                .getHostSetupNetworksParametersData()
                .addNetworkAttachmentToParameters(networkAttachment);
    }

    private NetworkAttachment createAttachmentWhenAttachingTo(VdsNetworkInterface targetNic) {
        NetworkAttachment networkAttachment =
                new NetworkAttachment(targetNic,
                        networkModel.getNetwork(),
                        NetworkCommonUtils.createDefaultIpConfiguration());

        NetworkParameters netParams =
                networkModel.getSetupModel().getNetworkToLastDetachParams().get(networkModel.getName());
        if (netParams != null) {
            applyOnAttachmentParamsFrom(netParams, networkAttachment);
        } else {
            VdsNetworkInterface nicToTakeParamsFrom = null;
            if (networkModel.hasVlan()) {
                nicToTakeParamsFrom = getPotentialVlanDevice(targetNic);
            } else {
                nicToTakeParamsFrom = targetNic;
            }

            boolean newlyCreatedBond = nicToTakeParamsFrom != null && nicToTakeParamsFrom.getId() == null;
            if (nicToTakeParamsFrom != null && !newlyCreatedBond) {
                InterfacePropertiesAccessor.FromNic interfacePropertiesAccessor =
                        new InterfacePropertiesAccessor.FromNic(nicToTakeParamsFrom, null);
                applyOnAttachmentParamsFrom(interfacePropertiesAccessor, networkAttachment);
            }

            fixBootProtocolOfMgmtNetworkIfNeeded(networkAttachment);
        }

        return networkAttachment;
    }

    private VdsNetworkInterface getPotentialVlanDevice(VdsNetworkInterface targetNic) {
        VdsNetworkInterface potentialVlanDeviceToAttachTo =
                networkModel.getSetupModel().getExistingVlanDeviceByVlanId(networkModel.getVlanId());
        if (potentialVlanDeviceToAttachTo != null
                && potentialVlanDeviceToAttachTo.getBaseInterface().equals(targetNic.getName())) {
            return potentialVlanDeviceToAttachTo;
        } else {
            return null;
        }
    }

    private void fixBootProtocolOfMgmtNetworkIfNeeded(NetworkAttachment networkAttachment) {
        IPv4Address ipV4address = networkAttachment.getIpConfiguration().getIpv4PrimaryAddress();
        if (networkModel.isManagement() && (ipV4address.getBootProtocol() == null
                || ipV4address.getBootProtocol() == Ipv4BootProtocol.NONE)) {
            ipV4address.setBootProtocol(Ipv4BootProtocol.DHCP);
        }
    }

    private void applyOnAttachmentParamsFrom(InterfacePropertiesAccessor interfacePropertiesAccessor,
            NetworkAttachment networkAttachment) {
        populateIpv4Details(interfacePropertiesAccessor, networkAttachment.getIpConfiguration().getIpv4PrimaryAddress());
        populateIpv6Details(interfacePropertiesAccessor, networkAttachment.getIpConfiguration().getIpv6PrimaryAddress());

        if (interfacePropertiesAccessor.isQosOverridden()) {
            AnonymousHostNetworkQos anonymousHostNetworkQos =
                    AnonymousHostNetworkQos.fromHostNetworkQos(interfacePropertiesAccessor.getHostNetworkQos());
            networkAttachment.setHostNetworkQos(anonymousHostNetworkQos);
        }

        networkAttachment.setProperties(interfacePropertiesAccessor.getCustomProperties());
        networkAttachment.setDnsResolverConfiguration(interfacePropertiesAccessor.getDnsResolverConfiguration());
    }

    public void updateParametersToDetach() {
        assert networkModel.getAttachedToNic() != null;

        storeAttachmentParamsBeforeDetach();
        networkModel.getSetupModel()
                .getHostSetupNetworksParametersData()
                .removeNetworkAttachmentFromParameters(networkModel.getNetworkAttachment());
        networkModel.getSetupModel().getHostSetupNetworksParametersData().getNetworksToSync().remove(networkModel.getName());
    }

    private void storeAttachmentParamsBeforeDetach() {
        NetworkAttachment networkAttachment = networkModel.getNetworkAttachment();
        if (networkAttachment == null) {
            return;
        }
        NetworkParameters netParams = new NetworkParameters();

        IPv4Address ipv4Address = networkAttachment.getIpConfiguration().getIpv4PrimaryAddress();
        if (ipv4Address != null) {
            netParams.setIpv4BootProtocol(ipv4Address.getBootProtocol());
            netParams.setIpv4Address(ipv4Address.getAddress());
            netParams.setIpv4Netmask(ipv4Address.getNetmask());
            netParams.setIpv4Gateway(ipv4Address.getGateway());
        }

        IpV6Address ipv6Address = networkAttachment.getIpConfiguration().getIpv6PrimaryAddress();
        if (ipv6Address != null) {
            netParams.setIpv6BootProtocol(ipv6Address.getBootProtocol());
            netParams.setIpv6Address(ipv6Address.getAddress());
            netParams.setIpv6Prefix(ipv6Address.getPrefix());
            netParams.setIpv6Gateway(ipv6Address.getGateway());
        }

        netParams.setHostNetworkQos(HostNetworkQos.fromAnonymousHostNetworkQos(networkAttachment.getHostNetworkQos()));

        netParams.setQosOverridden(networkAttachment.isQosOverridden());
        netParams.setCustomProperties(networkAttachment.getProperties());
        netParams.setDnsResolverConfiguration(networkAttachment.getDnsResolverConfiguration());

        networkModel.getSetupModel().getNetworkToLastDetachParams().put(networkModel.getName(), netParams);
    }

    public static void populateIpv4Details(InterfacePropertiesAccessor interfacePropertiesAccessor,
            IPv4Address ipv4Address) {
        final Ipv4BootProtocol ipv4BootProtocol = interfacePropertiesAccessor.getIpv4BootProtocol();
        ipv4Address.setBootProtocol(ipv4BootProtocol);
        final boolean staticBootProtocol = Ipv4BootProtocol.STATIC_IP == ipv4BootProtocol;
        ipv4Address.setAddress(staticBootProtocol ? interfacePropertiesAccessor.getIpv4Address() : null);
        ipv4Address.setNetmask(staticBootProtocol ? interfacePropertiesAccessor.getIpv4Netmask() : null);
        ipv4Address.setGateway(staticBootProtocol ? interfacePropertiesAccessor.getIpv4Gateway() : null);
    }

    public static void populateIpv6Details(InterfacePropertiesAccessor interfacePropertiesAccessor,
            IpV6Address ipv6Address) {
        final Ipv6BootProtocol ipv6BootProtocol = interfacePropertiesAccessor.getIpv6BootProtocol();
        ipv6Address.setBootProtocol(ipv6BootProtocol);
        final boolean staticBootProtocol = Ipv6BootProtocol.STATIC_IP == ipv6BootProtocol;
        ipv6Address.setAddress(staticBootProtocol ? interfacePropertiesAccessor.getIpv6Address() : null);
        ipv6Address.setPrefix(staticBootProtocol ? interfacePropertiesAccessor.getIpv6Prefix() : null);
        ipv6Address.setGateway(staticBootProtocol ? interfacePropertiesAccessor.getIpv6Gateway() : null);
    }
}
