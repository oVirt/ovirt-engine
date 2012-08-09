package org.ovirt.engine.ui.uicommonweb.models.hosts.network;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface.NetworkImplementationDetails;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.NetworkParameters;

/**
 * A Model for Logical Networks
 */
public class LogicalNetworkModel extends NetworkItemModel<NetworkStatus> {

    private boolean selected;
    private boolean management;
    private NetworkInterfaceModel attachedToNic;
    private NetworkInterfaceModel bridge;

    public LogicalNetworkModel(HostSetupNetworksModel setupModel) {
        super(setupModel);
    }

    public LogicalNetworkModel(Network network, HostSetupNetworksModel setupModel) {
        this(setupModel);
        setEntity(network);
        if (HostInterfaceListModel.ENGINE_NETWORK_NAME.equals(network.getname())) {
            setManagement(true);
        }
    }

    /**
     * attach a network to a target nic. If the network has VLAN id, it returns the newly created vlan bridge
     *
     * @param targetNic
     * @return
     */
    public VdsNetworkInterface attach(NetworkInterfaceModel targetNic, boolean createBridge) {
        attachedToNic = targetNic;
        List<LogicalNetworkModel> networksOnTarget = targetNic.getItems();
        networksOnTarget.add(this);

        NetworkParameters netParams = getSetupModel().getNetworkToLastDetachParams().get(getName());

        if (netParams != null && !hasVlan()){
            targetNic.getEntity().setBootProtocol(netParams.getBootProtocol());
            targetNic.getEntity().setAddress(netParams.getAddress());
            targetNic.getEntity().setSubnet(netParams.getSubnet());
            targetNic.getEntity().setGateway(netParams.getGateway());
        }

        if (isManagement()) {
            // mark the nic as a management nic
            targetNic.getEntity().setType(2);
        }
        if (!createBridge) {
            return null;
        }
        VdsNetworkInterface targetNicEntity = targetNic.getEntity();

        if (hasVlan()) {
            // create vlan bridge (eth0.1)
            VdsNetworkInterface bridge = new VdsNetworkInterface();
            bridge.setName(targetNic.getName() + "." + getVlanId()); //$NON-NLS-1$
            bridge.setNetworkName(getName());
            bridge.setVlanId(getVlanId());
            bridge.setMtu(getEntity().getMtu());
            bridge.setVdsId(targetNicEntity.getVdsId());
            bridge.setVdsName(targetNicEntity.getVdsName());
            if (netParams != null){
                bridge.setBootProtocol(netParams.getBootProtocol());
                bridge.setAddress(netParams.getAddress());
                bridge.setSubnet(netParams.getSubnet());
                bridge.setGateway(netParams.getGateway());
            }else{
                bridge.setBootProtocol(NetworkBootProtocol.None);
            }
            return bridge;
        } else {
            targetNicEntity.setNetworkName(getName());
            targetNicEntity.setMtu(getEntity().getMtu());
            return null;
        }
    }

    public void detach() {
        assert attachedToNic != null;
        NetworkInterfaceModel attachingNic = attachedToNic;
        // this needs to be null before the NIC items are changed, because they trigger an event
        attachedToNic = null;
        List<LogicalNetworkModel> nicNetworks = attachingNic.getItems();
        nicNetworks.remove(this);
        // clear network name
        VdsNetworkInterface nicEntity = attachingNic.getEntity();

        NetworkParameters netParams = new NetworkParameters();
        if (!hasVlan()){
            netParams.setBootProtocol(nicEntity.getBootProtocol());
            netParams.setAddress(nicEntity.getAddress());
            netParams.setSubnet(nicEntity.getSubnet());
        }else{
            netParams.setBootProtocol(bridge.getEntity().getBootProtocol());
            netParams.setAddress(bridge.getEntity().getAddress());
            netParams.setSubnet(bridge.getEntity().getSubnet());
        }

        if (isManagement()){
            netParams.setGateway(nicEntity.getGateway());
        }

        getSetupModel().getNetworkToLastDetachParams().put(getName(), netParams);

        if (!hasVlan()) {
            nicEntity.setNetworkName(null);
            nicEntity.setBootProtocol(null);
            nicEntity.setAddress(null);
            nicEntity.setSubnet(null);
            nicEntity.setGateway(null);
        }
        setBridge(null);
        // is this a management nic?
        if (nicEntity.getIsManagement()) {
            nicEntity.setType(0);
        }
    }

    public NetworkInterfaceModel getAttachedToNic() {
        return attachedToNic;
    }

    public NetworkInterfaceModel getBridge() {
        return bridge;
    }

    @Override
    public Network getEntity() {
        return (Network) super.getEntity();
    }

    @Override
    public String getName() {
        return getEntity().getname();
    }

    @Override
    public NetworkStatus getStatus() {
        return (getEntity().getCluster() == null ? null : getEntity().getCluster().getstatus());
    }

    public int getVlanId() {
        Integer vlanId = getEntity().getvlan_id();
        return vlanId == null ? 0 : vlanId;
    }

    public boolean hasVlan() {
        return getVlanId() > 0;
    }

    public boolean isAttached() {
        return attachedToNic != null;
    }

    public boolean isManagement() {
        return management;
    }

    public Boolean isSelected() {
        return selected;
    }

    public void setBridge(NetworkInterfaceModel bridge) {
        this.bridge = bridge;
    }

    public void setManagement(boolean management) {
        this.management = management;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }


    public boolean isInSync() {
        NetworkImplementationDetails details = getNetworkImplementationDetails();
        return details != null ? details.isInSync() : true;
    }

    public boolean isManaged(){
        NetworkImplementationDetails details = getNetworkImplementationDetails();
        return details != null? details.isManaged() : true;
    }

    public NetworkImplementationDetails getNetworkImplementationDetails(){
        if (!isAttached()){
            return null;
        }

        VdsNetworkInterface nic = hasVlan() ? getBridge().getEntity() : getAttachedToNic().getEntity();
        return nic.getNetworkImplementationDetails();
    }
}
