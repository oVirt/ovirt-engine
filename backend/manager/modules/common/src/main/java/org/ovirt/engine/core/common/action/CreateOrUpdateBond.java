package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class CreateOrUpdateBond implements BusinessEntity<Guid>, Nameable {
    private static final long serialVersionUID = 7535719902326461903L;

    private Guid id;

    @Size(min = 1, max = BusinessEntitiesDefinitions.BOND_NAME_SIZE)
    private String name;

    private String bondOptions;

    private Set<String> slaves = new HashSet<>();

    public boolean equalToBond(Bond bond) {
        return Objects.equals(getId(), bond.getId())
                && Objects.equals(getName(), bond.getName())
                && Objects.equals(getBondOptions(), bond.getBondOptions())
                && bondsHasSameSlaves(bond);
    }

    private boolean bondsHasSameSlaves(Bond existingNic) {
        Collection<String> slavesOfBondFromRequest = replaceNullWithEmptyList(getSlaves());
        Collection<String> slavesOfExistingBond = replaceNullWithEmptyList(existingNic.getSlaves());

        //bonds can be in any order, and I don't want to change this order during this check.
        return slavesOfBondFromRequest.size() == slavesOfExistingBond.size()
                && slavesOfBondFromRequest.containsAll(slavesOfExistingBond);
    }

    private Collection<String> replaceNullWithEmptyList(Collection<String> list) {
        return list == null ? Collections.emptyList() : list;
    }

    public Bond toBond() {
        Bond bond = new Bond();

        bond.setId(getId());
        bond.setName(getName());
        bond.setSlaves(new ArrayList<>(getSlaves()));
        bond.setBondOptions(getBondOptions());

        return bond;
    }

    public static CreateOrUpdateBond fromBond(Bond bond) {
        CreateOrUpdateBond createOrUpdateBond = new CreateOrUpdateBond();

        createOrUpdateBond.setId(bond.getId());
        createOrUpdateBond.setName(bond.getName());
        createOrUpdateBond.setSlaves(new HashSet<>(bond.getSlaves()));
        createOrUpdateBond.setBondOptions(bond.getBondOptions());

        return createOrUpdateBond;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CreateOrUpdateBond)) {
            return false;
        }
        CreateOrUpdateBond that = (CreateOrUpdateBond) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getBondOptions(), that.getBondOptions()) &&
                Objects.equals(getSlaves(), that.getSlaves());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getBondOptions(), getSlaves());
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBondOptions() {
        return bondOptions;
    }

    public void setBondOptions(String bondOptions) {
        this.bondOptions = bondOptions;
    }

    public Set<String> getSlaves() {
        return slaves;
    }

    public void setSlaves(Set<String> slaves) {
        this.slaves = slaves;
    }

    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return tsb.append("id", getId())
                .append("name", getName())
                .append("bondingOptions", getBondOptions())
                .append("slaves", getSlaves());
    }

    @Override
    public String toString() {
        return appendAttributes(ToStringBuilder.forInstance(this)).build();
    }
}
