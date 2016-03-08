package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
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

            if (nicToTakeParamsFrom != null) {
                applyOnAttachmentParamsFrom(new InterfacePropertiesAccessor.FromNic(nicToTakeParamsFrom),
                        networkAttachment);
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
        IPv4Address ipV4address = networkAttachment.getIpConfiguration().getIpv4PrimaryAddress();
        ipV4address.setBootProtocol(interfacePropertiesAccessor.getIpv4BootProtocol());
        ipV4address.setAddress(interfacePropertiesAccessor.getIpv4Address());
        ipV4address.setNetmask(interfacePropertiesAccessor.getIpv4Netmask());
        ipV4address.setGateway(interfacePropertiesAccessor.getIpv4Gateway());

        IpV6Address ipv6Address = networkAttachment.getIpConfiguration().getIpv6PrimaryAddress();
        ipv6Address.setBootProtocol(interfacePropertiesAccessor.getIpv6BootProtocol());
        ipv6Address.setAddress(interfacePropertiesAccessor.getIpv6Address());
        ipv6Address.setPrefix(interfacePropertiesAccessor.getIpv6Prefix());
        ipv6Address.setGateway(interfacePropertiesAccessor.getIpv6Gateway());

        if (interfacePropertiesAccessor.isQosOverridden()) {
            networkAttachment.setHostNetworkQos(interfacePropertiesAccessor.getHostNetworkQos());
        }

        networkAttachment.setProperties(interfacePropertiesAccessor.getCustomProperties());
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

        netParams.setHostNetworkQos(networkAttachment.getHostNetworkQos());

        netParams.setQosOverridden(networkAttachment.isQosOverridden());
        netParams.setCustomProperties(networkAttachment.getProperties());

        networkModel.getSetupModel().getNetworkToLastDetachParams().put(networkModel.getName(), netParams);
    }
}
