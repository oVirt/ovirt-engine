package org.ovirt.engine.core.common.businessentities.network;

import org.ovirt.engine.core.common.utils.ObjectUtils;

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
        StringBuilder builder = new StringBuilder();
        builder.append(getName())
                .append(" {id=")
                .append(getId())
                .append(", networkName=")
                .append(getNetworkName())
                .append(", vnicProfileName=")
                .append(getVnicProfileName())
                .append(", vnicProfileId=")
                .append(getVnicProfileId())
                .append(", speed=")
                .append(getSpeed())
                .append(", type=")
                .append(getType())
                .append(", macAddress=")
                .append(getMacAddress())
                .append(", active=")
                .append(isPlugged())
                .append(", linked=")
                .append(isLinked())
                .append(", portMirroring=")
                .append(isPortMirroring())
                .append(", vmId=")
                .append(getVmId())
                .append(", vmName=")
                .append(getVmName())
                .append(", vmTemplateId=")
                .append(getVmTemplateId())
                .append(", QoSName=")
                .append(getQosName())
                .append("}");
        return builder.toString();
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
