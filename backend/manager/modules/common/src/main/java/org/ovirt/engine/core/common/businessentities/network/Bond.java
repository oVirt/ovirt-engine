package org.ovirt.engine.core.common.businessentities.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.Pattern;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class Bond extends VdsNetworkInterface {

    private static final long serialVersionUID = 268337006285648461L;
    private List<String> slaves;

    public Bond() {
        setBonded(true);
        slaves = new ArrayList<>();
    }

    public Bond(String macAddress, String bondOptions, Integer bondType) {
        this();
        setMacAddress(macAddress);
        setBondOptions(bondOptions);
        setBondType(bondType);
    }

    public Bond(String name) {
        this();
        setName(name);
    }

    @Override
    @Pattern(regexp = BusinessEntitiesDefinitions.BOND_NAME_PATTERN, message = "NETWORK_BOND_NAME_BAD_FORMAT")
    public String getName() {
        return super.getName();
    }

    public List<String> getSlaves() {
        return slaves;
    }

    public void setSlaves(List<String> slaves) {
        this.slaves = slaves;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("macAddress", getMacAddress())
                .append("bondOptions", getBondOptions())
                .append("labels", getLabels())
                .append("slaves", getSlaves());
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
                && Objects.equals(slaves, other.slaves);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                getSlaves()
        );
    }
}
