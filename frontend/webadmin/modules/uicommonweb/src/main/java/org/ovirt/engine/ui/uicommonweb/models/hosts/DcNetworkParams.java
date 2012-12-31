package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.network.Network;

public class DcNetworkParams {

    private Integer vlanId;
    private int mtu;
    private boolean vmNetwork;

    public DcNetworkParams() {
    }

    public DcNetworkParams(Network network) {
        vlanId = network.getvlan_id();
        mtu = network.getMtu();
        vmNetwork = network.isVmNetwork();
    }

    public Integer getVlanId() {
        return vlanId;
    }
    public void setVlanId(Integer vlanId) {
        this.vlanId = vlanId;
    }
    public int getMtu() {
        return mtu;
    }
    public void setMtu(int mtu) {
        this.mtu = mtu;
    }
    public boolean isVmNetwork() {
        return vmNetwork;
    }
    public void setVmNetwork(boolean vmNetwork) {
        this.vmNetwork = vmNetwork;
    }


}
