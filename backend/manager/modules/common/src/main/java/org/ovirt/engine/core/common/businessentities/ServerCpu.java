package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.utils.CpuVendor;

public class ServerCpu implements Serializable {
    private static final long serialVersionUID = -267863982363067020L;

    public ServerCpu(String name, int level, Set<String> flags, String verbData, ArchitectureType architecture) {
        setCpuName(name);
        setLevel(level);
        setFlags(flags);
        setVdsVerbData(verbData);
        this.privateArchitecture = architecture;
    }

    private String privateCpuName;

    public String getCpuName() {
        return privateCpuName;
    }

    public void setCpuName(String value) {
        privateCpuName = value;
    }

    private int privateLevel;

    public int getLevel() {
        return privateLevel;
    }

    public void setLevel(int value) {
        privateLevel = value;
    }

    private Set<String> privateFlags;

    public Set<String> getFlags() {
        return privateFlags;
    }

    public void setFlags(Set<String> value) {
        privateFlags = value;
    }

    private String privateVdsVerbData;

    public String getVdsVerbData() {
        return privateVdsVerbData;
    }

    public void setVdsVerbData(String value) {
        privateVdsVerbData = value;
    }

    public ServerCpu() {
    }

    private ArchitectureType privateArchitecture;

    public ArchitectureType getArchitecture() {
        return privateArchitecture;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                privateArchitecture,
                privateCpuName,
                privateFlags,
                privateLevel,
                privateVdsVerbData
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ServerCpu)) {
            return false;
        }
        ServerCpu other = (ServerCpu) obj;
        return Objects.equals(privateArchitecture, other.privateArchitecture)
                && Objects.equals(privateCpuName, other.privateCpuName)
                && Objects.equals(privateFlags, other.privateFlags)
                && privateLevel == other.privateLevel
                && Objects.equals(privateVdsVerbData, other.privateVdsVerbData);
    }

    public CpuVendor getVendor() {
        return CpuVendor.fromFlags(privateFlags);
    }

    @Override
    public String toString() {
        return "ServerCpu [" + privateCpuName + "]";
    }
}
