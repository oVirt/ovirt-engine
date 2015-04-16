package org.ovirt.engine.core.common.businessentities.network;

import org.ovirt.engine.core.common.utils.ObjectUtils;
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
        setVmTemplateId(iface.getVmTemplateId());
        setStatistics(new VmNetworkStatistics(iface.getStatistics()));
    }

    @Override
    public Object getQueryableId() {
        return id;
    }

    /**
     * The network name the vnic is attached to
     *
     * @param networkName
     * @deprecated since ovirt 3.3 the network name is replaced by {@link VmNic.getVnicProfileId()} which determines the
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

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", getId())
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
                .append("vmTemplateId", getVmTemplateId())
                .append("QoSName", getQosName())
                .build();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((getNetworkName() == null) ? 0 : getNetworkName().hashCode());
        result = prime * result + ((getVnicProfileName() == null) ? 0 : getVnicProfileName().hashCode());
        result = prime * result + (isPortMirroring() ? 1231 : 1237);
        result = prime * result + ((getVmName() == null) ? 0 : getVmName().hashCode());
        result = prime * result + (isPlugged() ? 1231 : 1237);
        result = prime * result + ((getQosName() == null) ? 0 :getQosName().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof VmNetworkInterface)) {
            return false;
        }
        VmNetworkInterface other = (VmNetworkInterface) obj;
        if (!ObjectUtils.objectsEqual(other.getNetworkName(), other.getNetworkName())) {
            return false;
        }
        if (!ObjectUtils.objectsEqual(other.getVnicProfileName(), other.getVnicProfileName())) {
            return false;
        }
        if (isPortMirroring() != other.isPortMirroring()) {
            return false;
        }
        if (!ObjectUtils.objectsEqual(other.getVmName(), other.getVmName())) {
            return false;
        }
        if (isPlugged() != other.isPlugged()) {
            return false;
        }
        if (!ObjectUtils.objectsEqual(getQosName(), other.getQosName())) {
            return false;
        }

        return true;
    }
}
