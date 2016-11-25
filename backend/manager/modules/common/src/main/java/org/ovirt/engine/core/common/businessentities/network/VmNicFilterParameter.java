package org.ovirt.engine.core.common.businessentities.network;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;



/**
 * <code>VmNicFilterParameter</code> defines a name value pair of parameters for {@link NetworkFilter}
 * for instances of {@link VmNic}.
 */
public class VmNicFilterParameter implements BusinessEntity<Guid>, Nameable {

    private Guid id;
    private Guid vmInterfaceId;
    private String name;
    private String value;

    public VmNicFilterParameter() {
        this.id = Guid.Empty;
    }

    public VmNicFilterParameter(Guid id, Guid vmInterfaceId, String name, String value) {
        this.id = id;
        this.vmInterfaceId = vmInterfaceId;
        this.name = name;
        this.value = value;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public Guid getVmInterfaceId() {
        return vmInterfaceId;
    }

    public void setVmInterfaceId(Guid vmInterfaceId) {
        this.vmInterfaceId = vmInterfaceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, vmInterfaceId, name, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VmNicFilterParameter)) {
            return false;
        }
        VmNicFilterParameter that = (VmNicFilterParameter) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(vmInterfaceId, that.vmInterfaceId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(value, that.value);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", getId())
                .append("vmInterfaceId", getVmInterfaceId())
                .append("name", getName())
                .append("value", getValue())
                .build();
    }
}
