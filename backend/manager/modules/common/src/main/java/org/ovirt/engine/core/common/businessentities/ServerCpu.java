package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;

public class ServerCpu implements Serializable {
    private static final long serialVersionUID = -267863982363067020L;

    public ServerCpu(String name, int level, HashSet<String> flags, String verbData, ArchitectureType architecture) {
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

    private HashSet<String> privateFlags;

    public HashSet<String> getFlags() {
        return privateFlags;
    }

    public void setFlags(HashSet<String> value) {
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((privateArchitecture == null) ? 0 : privateArchitecture.hashCode());
        result = prime * result + ((privateCpuName == null) ? 0 : privateCpuName.hashCode());
        result = prime * result + ((privateFlags == null) ? 0 : privateFlags.hashCode());
        result = prime * result + privateLevel;
        result = prime * result + ((privateVdsVerbData == null) ? 0 : privateVdsVerbData.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ServerCpu other = (ServerCpu) obj;
        return Objects.equals(privateArchitecture, other.privateArchitecture)
                && Objects.equals(privateCpuName, other.privateCpuName)
                && Objects.equals(privateFlags, other.privateFlags)
                && privateLevel == other.privateLevel
                && Objects.equals(privateVdsVerbData, other.privateVdsVerbData);
    }

}
