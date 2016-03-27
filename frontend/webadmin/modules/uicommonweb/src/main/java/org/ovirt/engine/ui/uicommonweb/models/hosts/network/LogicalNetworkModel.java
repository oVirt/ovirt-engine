package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurations;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;

/**
 * A Model for Logical Networks
 */
public class LogicalNetworkModel extends NetworkItemModel<NetworkStatus> {

    private final boolean management;
    private boolean attachedViaLabel;
    private String errorMessage;
    private NetworkInterfaceModel attachedToNic;
    private VdsNetworkInterface vlanDevice;
    private Network network;
    private NetworkAttachment networkAttachment;

    public LogicalNetworkModel(Network network,
            NetworkAttachment networkAttachment,
            HostSetupNetworksModel setupModel) {
        super(setupModel);
        this.network = network;
        this.networkAttachment = networkAttachment;
        management = network.getCluster() != null && network.getCluster().isManagement();
    }

    public void attach(NetworkInterfaceModel targetNic) {
        attachedToNic = targetNic;
        List<LogicalNetworkModel> networksOnTarget = targetNic.getItems();
        networksOnTarget.add(this);
    }

    public NetworkInterfaceModel getAttachedToNic() {
        return attachedToNic;
    }

    public VdsNetworkInterface getVlanDevice() {
        return vlanDevice;
    }

    public Network getNetwork() {
        return network;
    }

    @Override
    public String getName() {
        return getNetwork().getName();
    }

    @Override
    public NetworkStatus getStatus() {
        return getNetwork().getCluster() == null ? null : getNetwork().getCluster().getStatus();
    }

    public int getVlanId() {
        Integer vlanId = getNetwork().getVlanId();
        return vlanId == null ? -1 : vlanId;
    }

    public boolean hasVlan() {
        return getVlanId() >= 0;
    }

    public boolean isAttached() {
        return attachedToNic != null;
    }

    public boolean isAttachedViaLabel() {
        return attachedViaLabel;
    }

    public void attachViaLabel() {
        attachedViaLabel = true;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isManagement() {
        return management;
    }

    public void setVlanDevice(VdsNetworkInterface vlanDevice) {
        this.vlanDevice = vlanDevice;
    }

    public boolean isInSync() {
        ReportedConfigurations reportedConfigurations = getReportedConfigurations();
        return reportedConfigurations == null || reportedConfigurations.isNetworkInSync();
    }

    public boolean isManaged() {
        return !(isAttached() && getNetworkAttachment() == null);
    }

    public ReportedConfigurations getReportedConfigurations() {
        NetworkAttachment networkAttachment = getNetworkAttachment();
        return networkAttachment == null ? null : networkAttachment.getReportedConfigurations();
    }

    @Override
    public String getType() {
        return HostSetupNetworksModel.NETWORK;
    }

    public NetworkAttachment getNetworkAttachment() {
        return networkAttachment;
    }
}
