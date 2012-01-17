package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ServerCpu")
public class ServerCpu implements Serializable {
    private static final long serialVersionUID = -267863982363067020L;

    public ServerCpu(String name, int level, java.util.HashSet<String> flags, String verbData) {
        setCpuName(name);
        setLevel(level);
        setFlags(flags);
        setVdsVerbData(verbData);
    }

    @XmlElement(name = "CpuName")
    private String privateCpuName;

    public String getCpuName() {
        return privateCpuName;
    }

    public void setCpuName(String value) {
        privateCpuName = value;
    }

    @XmlElement(name = "Level")
    private int privateLevel;

    public int getLevel() {
        return privateLevel;
    }

    public void setLevel(int value) {
        privateLevel = value;
    }

    @XmlElement(name = "Flags")
    private java.util.HashSet<String> privateFlags;

    public java.util.HashSet<String> getFlags() {
        return privateFlags;
    }

    public void setFlags(java.util.HashSet<String> value) {
        privateFlags = value;
    }

    @XmlElement(name = "VdsVerbData")
    private String privateVdsVerbData;

    public String getVdsVerbData() {
        return privateVdsVerbData;
    }

    public void setVdsVerbData(String value) {
        privateVdsVerbData = value;
    }

    public ServerCpu() {
    }
}
