package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class VdsCpuUnit implements Comparable<VdsCpuUnit>, Serializable, Cloneable {

    private static final long serialVersionUID = -397254497953366129L;

    private int numa;

    private int socket;

    private int core;

    private int cpu;

    @JsonIgnore
    private List<Guid> vmIds;
    @JsonIgnore
    private CpuPinningPolicy cpuPinningPolicy;

    public VdsCpuUnit() {
        this.cpuPinningPolicy = CpuPinningPolicy.NONE;
        this.vmIds = new ArrayList<>();
    }

    public VdsCpuUnit(int numa, int socket, int core, int cpu) {
        this.numa = numa;
        this.socket = socket;
        this.core = core;
        this.cpu = cpu;
        this.cpuPinningPolicy = CpuPinningPolicy.NONE;
        this.vmIds = new ArrayList<>();
    }

    public VdsCpuUnit(VdsCpuUnit vdsCpuUnit) {
        this.numa = vdsCpuUnit.getNuma();
        this.socket = vdsCpuUnit.getSocket();
        this.core = vdsCpuUnit.getCore();
        this.cpu = vdsCpuUnit.getCpu();
        this.vmIds = vdsCpuUnit.getVmIds();
        this.cpuPinningPolicy = vdsCpuUnit.getCpuPinningPolicy();
    }

    public int getSocket() {
        return socket;
    }

    public void setSocket(int socket) {
        this.socket = socket;
    }

    public int getNuma() {
        return numa;
    }

    public void setNuma(int numa) {
        this.numa = numa;
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
    public void setVmIds(List<Guid> vmIds) {
        this.vmIds = vmIds;
    }

    @JsonIgnore
    public List<Guid> getVmIds() {
        return vmIds;
    }

    @JsonIgnore
    public boolean isPinned() {
        return cpuPinningPolicy != CpuPinningPolicy.NONE;
    }

    @JsonIgnore
    public void setCpuPinningPolicy(CpuPinningPolicy cpuPinningPolicy) {
        this.cpuPinningPolicy = cpuPinningPolicy;
    }

    @JsonIgnore
    public CpuPinningPolicy getCpuPinningPolicy() {
        return cpuPinningPolicy;
    }

    @JsonIgnore
    public boolean isExclusive() {
        return cpuPinningPolicy.isExclusive();
    }

    @JsonIgnore
    public boolean isManual() {
        return cpuPinningPolicy == CpuPinningPolicy.MANUAL;
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
        return socket == other.socket && numa == other.numa && core == other.core && cpu == other.cpu;
    }

    @Override
    public int hashCode() {
        return Objects.hash(socket, core, cpu);
    }

    @Override
    public int compareTo(VdsCpuUnit cpuUnit) {
        int res;
        res = Integer.compare(getSocket(), cpuUnit.getSocket());
        if (res == 0) {
            res = Integer.compare(getCore(), cpuUnit.getCore());
            if (res == 0) {
                return Integer.compare(getCpu(), cpuUnit.getCpu());
            }
        }
        return res;
    }


    public boolean pinVm(Guid vmId, CpuPinningPolicy cpuPinningPolicy) {
        if (this.isExclusive() || this.isPinned() && cpuPinningPolicy.isExclusive()) {
            return false;
        }
        if (!this.vmIds.contains(vmId)) {
            this.vmIds.add(vmId);
        }
        // CpuPinningPolicy.NONE may happen when the engine generates CPU pinning based on the NUMA pinning.
        if (cpuPinningPolicy == CpuPinningPolicy.RESIZE_AND_PIN_NUMA || cpuPinningPolicy == CpuPinningPolicy.NONE) {
            cpuPinningPolicy = CpuPinningPolicy.MANUAL;
        }
        this.cpuPinningPolicy = cpuPinningPolicy;
        return true;
    }

    public boolean unPinVm(Guid vmId) {
        this.vmIds.remove(vmId);
        if (this.vmIds.isEmpty()) {
            this.cpuPinningPolicy = CpuPinningPolicy.NONE;
        }
        return true;
    }

    public VdsCpuUnit clone() {
        VdsCpuUnit clone = new VdsCpuUnit(this);
        List<Guid> vmIds = new ArrayList<>(this.getVmIds());
        clone.setVmIds(vmIds);
        return clone;
    }
}
