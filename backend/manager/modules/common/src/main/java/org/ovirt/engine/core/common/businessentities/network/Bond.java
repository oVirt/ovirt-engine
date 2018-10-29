package org.ovirt.engine.core.common.businessentities.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class Bond extends VdsNetworkInterface {

    private static final long serialVersionUID = 268337006285648461L;
    private List<String> slaves = new ArrayList<>();
    private String activeSlave;

    public Bond() {
        setBonded(true);
    }

    public Bond(String name) {
        this();
        setName(name);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    public List<String> getSlaves() {
        return slaves;
    }

    public void setSlaves(List<String> slaves) {
        this.slaves = slaves;
    }

    /**
     * Returns the name of the active slave interface in bond.
     *
     * @return the name of the active slave interface in bond
     */
    public String getActiveSlave() {
        return activeSlave;
    }

    /**
     * Sets the name of the active slave interface in bond.
     *
     * @param activeSlave
     *            name of the active slave interface in bond
     */
    public void setActiveSlave(String activeSlave) {
        // No validation here to propagate even invalid values
        this.activeSlave = activeSlave;
    }

    @Override
    public void setBonded(Boolean bonded) {
        if (Objects.equals(Boolean.FALSE, bonded)) {
            throw new IllegalArgumentException("It is illegal to have 'unbonded' bond.");
        } else {
            super.setBonded(bonded);
        }
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("macAddress", getMacAddress())
                .append("bondOptions", getBondOptions())
                .append("labels", getLabels())
                .append("slaves", getSlaves())
                .append("activeSlave", getActiveSlave());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Bond)) {
            return false;
        }
        Bond other = (Bond) obj;
        return super.equals(obj)
                && Objects.equals(slaves, other.slaves)
                && Objects.equals(activeSlave, other.activeSlave);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                getSlaves(),
                getActiveSlave()
        );
    }

}
