package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
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
        IPv4Address ipV4address = networkAttachment.getIpConfiguration().getPrimaryAddress();
        if (networkModel.isManagement() && (ipV4address.getBootProtocol() == null
                || ipV4address.getBootProtocol() == NetworkBootProtocol.NONE)) {
            ipV4address.setBootProtocol(NetworkBootProtocol.DHCP);
        }
    }

    private void applyOnAttachmentParamsFrom(InterfacePropertiesAccessor interfacePropertiesAccessor,
            NetworkAttachment networkAttachment) {
        IPv4Address ipV4address = networkAttachment.getIpConfiguration().getPrimaryAddress();
        ipV4address.setBootProtocol(interfacePropertiesAccessor.getBootProtocol());
        ipV4address.setAddress(interfacePropertiesAccessor.getAddress());
        ipV4address.setNetmask(interfacePropertiesAccessor.getNetmask());
        ipV4address.setGateway(interfacePropertiesAccessor.getGateway());

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

        IPv4Address ipAdrdress = networkAttachment.getIpConfiguration().getPrimaryAddress();

        if (ipAdrdress != null) {
            netParams.setBootProtocol(ipAdrdress.getBootProtocol());
            netParams.setAddress(ipAdrdress.getAddress());
            netParams.setNetmask(ipAdrdress.getNetmask());
            netParams.setGateway(ipAdrdress.getGateway());
        }

        netParams.setHostNetworkQos(networkAttachment.getHostNetworkQos());

        netParams.setQosOverridden(networkAttachment.isQosOverridden());
        netParams.setCustomProperties(networkAttachment.getProperties());

        networkModel.getSetupModel().getNetworkToLastDetachParams().put(networkModel.getName(), netParams);
    }
}
