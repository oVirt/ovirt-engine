package org.ovirt.engine.core.common.businessentities.network;

import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

/**
 * <code>VmNetworkInterface</code> defines a type of {@link VmNic} for instances of {@link VM}.
 */
public class VmNetworkInterface extends VmNic {

    private static final long serialVersionUID = -6110269859408208756L;
    private String networkName;
    private String vnicProfileName;
    private boolean portMirroring;
    private String vmName;
    private boolean plugged;
    private String qosName;
    private String failoverVnicProfileName;

    /** when this object represents a NIC that is attached to VM which is defined
     *  in a remote environment, this field contains the name of the network in
     *  the remote environment which the NIC is connected to */
    private String remoteNetworkName;

    public VmNetworkInterface() {
        plugged = true;
    }

    public VmNetworkInterface(VmNetworkInterface iface) {
        setId(iface.getId());
        setMacAddress(iface.getMacAddress());
        setName(iface.getName());
        setNetworkName(iface.getNetworkName());
        setLinked(iface.isLinked());
        setSpeed(iface.getSpeed());
        setType(iface.getType());
        setVmId(iface.getVmId());
        setVmName(iface.getVmName());
        setStatistics(new VmNetworkStatistics(iface.getStatistics()));
        setFailoverVnicProfileName(iface.getFailoverVnicProfileName());
    }

    @Override
    public Object getQueryableId() {
        return id;
    }

    /**
     * The network name the vnic is attached to
     *
     * @deprecated since ovirt 3.3 the network name is replaced by {@link VmNic#getVnicProfileId()} which determines the
     *             vnic profile used for using the network
     */
    @Deprecated
    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getNetworkName() {
        return networkName;
    }

    public String getVnicProfileName() {
        return vnicProfileName;
    }

    public String getQosName() {
        return qosName;
    }

    public void setVnicProfileName(String vnicProfileName) {
        this.vnicProfileName = vnicProfileName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public String getVmName() {
        return vmName;
    }

    public boolean isPortMirroring() {
        return portMirroring;
    }

    public void setPortMirroring(boolean portMirroring) {
        this.portMirroring = portMirroring;
    }

    public boolean isPlugged() {
        return plugged;
    }

    public void setPlugged(boolean plugged) {
        this.plugged = plugged;
    }

    public void setQosName(String qosName) {
        this.qosName = qosName;
    }

    public String getRemoteNetworkName() {
        return remoteNetworkName;
    }

    public void setRemoteNetworkName(String remoteNetworkName) {
        this.remoteNetworkName = remoteNetworkName;
    }

    public String getFailoverVnicProfileName() {
        return failoverVnicProfileName;
    }

    public void setFailoverVnicProfileName(String failoverVnicProfileName) {
        this.failoverVnicProfileName = failoverVnicProfileName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", getId())
                .append("name", getName())
                .append("networkName", getNetworkName())
                .append("vnicProfileName", getVnicProfileName())
                .append("vnicProfileId", getVnicProfileId())
                .append("speed", getSpeed())
                .append("type", getType())
                .append("macAddress", getMacAddress())
                .append("active", isPlugged())
                .append("linked", isLinked())
                .append("portMirroring", isPortMirroring())
                .append("vmId", getVmId())
                .append("vmName", getVmName())
                .append("QoSName", getQosName())
                .append("failoverVnicProfileName", getFailoverVnicProfileName())
                .append("remoteNetworkName", getRemoteNetworkName())
                .build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                networkName,
                vnicProfileName,
                portMirroring,
                vmName,
                plugged,
                qosName,
                failoverVnicProfileName
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VmNetworkInterface)) {
            return false;
        }
        VmNetworkInterface other = (VmNetworkInterface) obj;
        return super.equals(obj)
                && Objects.equals(networkName, other.networkName)
                && Objects.equals(vnicProfileName, other.vnicProfileName)
                && portMirroring == other.portMirroring
                && Objects.equals(vmName, other.vmName)
                && plugged == other.plugged
                && Objects.equals(qosName, other.qosName)
                && Objects.equals(failoverVnicProfileName, other.failoverVnicProfileName);
    }
}
