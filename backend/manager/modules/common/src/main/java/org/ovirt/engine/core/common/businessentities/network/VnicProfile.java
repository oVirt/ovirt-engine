package org.ovirt.engine.core.common.businessentities.network;

import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class VnicProfile implements Queryable, BusinessEntity<Guid>, Nameable {
    private static final long serialVersionUID = 1019016330475623259L;

    @NotNull(groups = { UpdateEntity.class, RemoveEntity.class })
    private Guid id;
    @Size(max = BusinessEntitiesDefinitions.NETWORK_NAME_SIZE)
    private String name;
    @NotNull(groups = { CreateEntity.class, UpdateEntity.class })
    private Guid networkId;
    private Guid networkQosId;

    private boolean portMirroring;
    private boolean passthrough;
    private boolean migratable;
    private String description;
    private Map<String, String> customProperties;
    private Guid networkFilterId;
    private Guid failoverVnicProfileId;

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

    public boolean isPortMirroring() {
        return portMirroring;
    }

    public void setPortMirroring(boolean portMirroring) {
        this.portMirroring = portMirroring;
    }

    public boolean isPassthrough() {
        return passthrough;
    }

    public void setPassthrough(boolean passthrough) {
        this.passthrough = passthrough;
    }

    public boolean isMigratable() {
        return migratable;
    }

    public void setMigratable(boolean migratable) {
        this.migratable = migratable;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }

    public Guid getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Guid networkId) {
        this.networkId = networkId;
    }

    public Guid getNetworkQosId() {
        return networkQosId;
    }

    public void setNetworkQosId(Guid networkQosId) {
        this.networkQosId = networkQosId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Guid getNetworkFilterId() {
        return networkFilterId;
    }

    public void setNetworkFilterId(Guid networkFilterId) {
        this.networkFilterId = networkFilterId;
    }

    public Guid getFailoverVnicProfileId() {
        return failoverVnicProfileId;
    }

    public void setFailoverVnicProfileId(Guid failoverVnicProfileId) {
        this.failoverVnicProfileId = failoverVnicProfileId;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                customProperties,
                id,
                name,
                networkId,
                networkQosId,
                portMirroring,
                passthrough,
                migratable,
                description,
                networkFilterId,
                failoverVnicProfileId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VnicProfile)) {
            return false;
        }
        VnicProfile other = (VnicProfile) obj;
        return Objects.equals(customProperties, other.customProperties)
                && Objects.equals(id, other.id)
                && Objects.equals(name, other.name)
                && Objects.equals(networkId, other.networkId)
                && Objects.equals(networkQosId, other.networkQosId)
                && portMirroring == other.portMirroring
                && passthrough == other.passthrough
                && migratable == other.migratable
                && Objects.equals(description, other.description)
                && Objects.equals(networkFilterId, other.networkFilterId)
                && Objects.equals(failoverVnicProfileId, other.failoverVnicProfileId);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", getId())
                .append("networkId", getNetworkId())
                .append("networkQosId", getNetworkQosId())
                .append("portMirroring", isPortMirroring())
                .append("passthrough", isPassthrough())
                .append("migratable", isMigratable())
                .append("customProperties", getCustomProperties())
                .append("description", getDescription())
                .append("networkFilterId", getNetworkFilterId())
                .append("failoverVnicProfileId", getFailoverVnicProfileId())
                .build();
    }
}
