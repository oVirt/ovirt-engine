package org.ovirt.engine.core.common.businessentities.network;

import java.util.Map;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.annotation.NetworkIdOrNetworkNameIsSet;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.RemoveEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

@NetworkIdOrNetworkNameIsSet(groups = { CreateEntity.class, UpdateEntity.class })
public class NetworkAttachment implements IVdcQueryable, BusinessEntity<Guid> {

    private static final long serialVersionUID = -8052325342869681284L;

    @NotNull(groups = { UpdateEntity.class, RemoveEntity.class })
    private Guid id;

    private Guid networkId;

    private String networkName;

    private Guid nicId;

    @Size(min = 1,
            max = BusinessEntitiesDefinitions.HOST_NIC_NAME_LENGTH,
            groups = { CreateEntity.class, UpdateEntity.class })
    private String nicName;

    @Valid
    private IpConfiguration ipConfiguration;

    private Map<String, String> properties;
    private boolean overrideConfiguration;
    private ReportedConfigurations reportedConfigurations;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public Guid getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Guid networkId) {
        this.networkId = networkId;
    }

    public Guid getNicId() {
        return nicId;
    }

    public void setNicId(Guid nicId) {
        this.nicId = nicId;
    }

    public String getNicName() {
        return nicName;
    }

    public void setNicName(String nicName) {
        this.nicName = nicName;
    }

    public IpConfiguration getIpConfiguration() {
        return ipConfiguration;
    }

    public void setIpConfiguration(IpConfiguration ipConfiguration) {
        this.ipConfiguration = ipConfiguration;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public boolean hasProperties() {
        return !(getProperties() == null || getProperties().isEmpty());
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public boolean isOverrideConfiguration() {
        return overrideConfiguration;
    }

    public void setOverrideConfiguration(boolean overrideConfiguration) {
        this.overrideConfiguration = overrideConfiguration;
    }

    public void setReportedConfigurations(ReportedConfigurations reportedConfigurations) {
        this.reportedConfigurations = reportedConfigurations;
    }

    public ReportedConfigurations getReportedConfigurations() {
        return reportedConfigurations;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof NetworkAttachment))
            return false;
        NetworkAttachment that = (NetworkAttachment) o;
        return Objects.equals(getNetworkId(), that.getNetworkId()) &&
                Objects.equals(getNicId(), that.getNicId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNetworkId(), getNicId());
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", getId())
                .append("networkId", getNetworkId())
                .append("networkName", getNetworkName())
                .append("nicId", getNicId())
                .append("nicName", getNicName())
                .append("ipConfiguration", getIpConfiguration())
                .append("properties", getProperties())
                .append("overrideConfiguration", isOverrideConfiguration())
                .build();
    }
}
