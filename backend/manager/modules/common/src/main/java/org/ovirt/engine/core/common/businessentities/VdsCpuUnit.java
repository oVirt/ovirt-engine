package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class VdsCpuUnit implements Serializable {

    private static final long serialVersionUID = -397254497953366129L;
    private int core;
    private int socket;
    private int cpu;
    @JsonIgnore
    private Guid vmId;

    public VdsCpuUnit() {

    }

    public VdsCpuUnit(int socket, int core, int cpu) {
        this.socket = socket;
        this.core = core;
        this.cpu = cpu;
    }

    public int getSocket() {
        return socket;
    }

    public void setSocket(int socket) {
        this.socket = socket;
    }

    public int getCore() {
        return core;
    }

    public void setCore(int core) {
        this.core = core;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    @JsonIgnore
    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    @JsonIgnore
    public Guid getVmId() {
        return vmId;
    }

    @JsonIgnore
    public boolean isPinned() {
        return !Guid.isNullOrEmpty(vmId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VdsCpuUnit)) {
            return false;
        }
        VdsCpuUnit other = (VdsCpuUnit) obj;
        return socket == other.socket && core == other.core && cpu == other.cpu;
    }

    @Override
    public int hashCode() {
        return Objects.hash(socket, core, cpu);
    }
}
