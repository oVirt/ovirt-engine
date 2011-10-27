package org.ovirt.engine.core.common.vdscommands;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.compat.Guid;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "NetworkVdsmVDSCommandParameters")
public class NetworkVdsmVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "NetworkName")
    private String privateNetworkName;

    public String getNetworkName() {
        return privateNetworkName;
    }

    private void setNetworkName(String value) {
        privateNetworkName = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "OldNetworkName")
    private String privateOldNetworkName;

    public String getOldNetworkName() {
        return privateOldNetworkName;
    }

    public void setOldNetworkName(String value) {
        privateOldNetworkName = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "HostAddr")
    private String privateHostAddr;

    public String getHostAddr() {
        return privateHostAddr;
    }

    public void setHostAddr(String value) {
        privateHostAddr = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "CheckConnectivity")
    private boolean privateCheckConnectivity;

    public boolean getCheckConnectivity() {
        return privateCheckConnectivity;
    }

    public void setCheckConnectivity(boolean value) {
        privateCheckConnectivity = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "ConnectionTimeout")
    private int privateConnectionTimeout;

    public int getConnectionTimeout() {
        return privateConnectionTimeout;
    }

    public void setConnectionTimeout(int value) {
        privateConnectionTimeout = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "VlanId")
    private Integer privateVlanId;

    public Integer getVlanId() {
        return privateVlanId;
    }

    private void setVlanId(Integer value) {
        privateVlanId = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "BondName")
    private String privateBondName;

    public String getBondName() {
        return privateBondName;
    }

    private void setBondName(String value) {
        privateBondName = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Nics")
    private String[] privateNics;

    public String[] getNics() {
        return privateNics == null ? new String[0] : privateNics;
    }

    private void setNics(String[] value) {
        privateNics = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "InetAddr")
    private String privateInetAddr;

    public String getInetAddr() {
        return privateInetAddr;
    }

    private void setInetAddr(String value) {
        privateInetAddr = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "NetworkMask")
    private String privateNetworkMask;

    public String getNetworkMask() {
        return privateNetworkMask;
    }

    private void setNetworkMask(String value) {
        privateNetworkMask = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Gateway")
    private String privateGateway;

    public String getGateway() {
        return privateGateway;
    }

    private void setGateway(String value) {
        privateGateway = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Stp")
    private boolean privateStp;

    public boolean getStp() {
        return privateStp;
    }

    private void setStp(boolean value) {
        privateStp = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "BondingOptions")
    private String privateBondingOptions;

    public String getBondingOptions() {
        return privateBondingOptions;
    }

    private void setBondingOptions(String value) {
        privateBondingOptions = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "BootProtocol")
    private NetworkBootProtocol privateBootProtocol = NetworkBootProtocol.forValue(0);

    public NetworkBootProtocol getBootProtocol() {
        return privateBootProtocol;
    }

    private void setBootProtocol(NetworkBootProtocol value) {
        privateBootProtocol = value;
    }

    public NetworkVdsmVDSCommandParameters(Guid vdsId, String networkName, Integer vlanId, String bondName,
            String[] nics, String inetAddr, String netMask, String gateway, boolean stp, String bondingOptions,
            NetworkBootProtocol bootProtocol) {
        super(vdsId);
        setNetworkName(networkName);
        setVlanId(vlanId);
        setBondName(bondName);
        setNics(nics);
        setInetAddr(inetAddr);
        setNetworkMask(netMask);
        setGateway(gateway);
        this.setStp(stp);
        this.setBondingOptions(bondingOptions);
        this.setBootProtocol(bootProtocol);
    }

    public NetworkVdsmVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, networkName=%s, oldNetworkName=%s, hostAddr=%s, checkConnectivity=%s, " +
                "connectionTimeout=%s, vlanId=%s, bondName=%s, nics=%s, inetAddr=%s, networkMask=%s, gateway=%s, " +
                "stp=%s, bondingOptions=%s, bootProtocol=%s",
                super.toString(),
                getNetworkName(),
                getOldNetworkName(),
                getHostAddr(),
                getCheckConnectivity(),
                getConnectionTimeout(),
                getVlanId(),
                getBondName(),
                Arrays.toString(getNics()),
                getInetAddr(),
                getNetworkMask(),
                getGateway(),
                getStp(),
                getBondingOptions(),
                getBootProtocol());
    }
}
