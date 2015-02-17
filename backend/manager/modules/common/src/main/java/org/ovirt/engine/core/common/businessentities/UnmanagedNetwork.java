package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class UnmanagedNetwork implements BusinessEntity<String> {

    private static final long serialVersionUID = -7952435653821354188L;

    private String networkName;
    private String nicName;
    private Guid nicId;

    @Override
    public String getId() {
        return getNetworkName();
    }

    @Override
    public void setId(String id) {
        setNetworkName(id);
    }

    public String getNetworkName() {
        return networkName;
    }

    public UnmanagedNetwork setNetworkName(String networkName) {
        this.networkName = networkName;
        return this;
    }

    public String getNicName() {
        return nicName;
    }

    public UnmanagedNetwork setNicName(String nicName) {
        this.nicName = nicName;
        return this;
    }

    public Guid getNicId() {
        return nicId;
    }

    public UnmanagedNetwork setNicId(Guid nicId) {
        this.nicId = nicId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof UnmanagedNetwork)) {
            return false;
        }

        UnmanagedNetwork that = (UnmanagedNetwork) o;
        return Objects.equals(getNetworkName(), that.getNetworkName()) &&
            Objects.equals(getNicName(), that.getNicName()) &&
            Objects.equals(getNicId(), that.getNicId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNetworkName(), getNicName(), getNicId());
    }
}
