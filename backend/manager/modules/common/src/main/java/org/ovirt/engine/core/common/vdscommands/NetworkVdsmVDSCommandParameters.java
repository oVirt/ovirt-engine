package org.ovirt.engine.core.common.vdscommands;

import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.compat.Guid;

public class NetworkVdsmVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private String privateNetworkName;

    public String getNetworkName() {
        return privateNetworkName;
    }

    private void setNetworkName(String value) {
        privateNetworkName = value;
    }

    private String privateOldNetworkName;

    public String getOldNetworkName() {
        return privateOldNetworkName;
    }

    public void setOldNetworkName(String value) {
        privateOldNetworkName = value;
    }

    private String privateHostAddr;

    public String getHostAddr() {
        return privateHostAddr;
    }

    public void setHostAddr(String value) {
        privateHostAddr = value;
    }

    private boolean privateCheckConnectivity;

    public boolean getCheckConnectivity() {
        return privateCheckConnectivity;
    }

    public void setCheckConnectivity(boolean value) {
        privateCheckConnectivity = value;
    }

    private int privateConnectionTimeout;

    public int getConnectionTimeout() {
        return privateConnectionTimeout;
    }

    public void setConnectionTimeout(int value) {
        privateConnectionTimeout = value;
    }

    private Integer privateVlanId;

    public Integer getVlanId() {
        return privateVlanId;
    }

    private void setVlanId(Integer value) {
        privateVlanId = value;
    }

    private String privateBondName;

    public String getBondName() {
        return privateBondName;
    }

    private void setBondName(String value) {
        privateBondName = value;
    }

    private String[] privateNics;

    public String[] getNics() {
        return privateNics == null ? new String[0] : privateNics;
    }

    private void setNics(String[] value) {
        privateNics = value;
    }

    private String privateInetAddr;

    public String getInetAddr() {
        return privateInetAddr;
    }

    private void setInetAddr(String value) {
        privateInetAddr = value;
    }

    private String privateNetworkMask;

    public String getNetworkMask() {
        return privateNetworkMask;
    }

    private void setNetworkMask(String value) {
        privateNetworkMask = value;
    }

    private String privateGateway;

    public String getGateway() {
        return privateGateway;
    }

    private void setGateway(String value) {
        privateGateway = value;
    }

    private boolean privateStp;

    public boolean getStp() {
        return privateStp;
    }

    private void setStp(boolean value) {
        privateStp = value;
    }

    private String privateBondingOptions;

    public String getBondingOptions() {
        return privateBondingOptions;
    }

    private void setBondingOptions(String value) {
        privateBondingOptions = value;
    }

    private NetworkBootProtocol privateBootProtocol = NetworkBootProtocol.forValue(0);
    private boolean vmNetwork;
    private network network;

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

    /**
     * use this constructor to pass a network object that holds other 'logical' properties such as mtu, vmNetwork and so
     * on...
     * @param vdsId
     * @param net
     * @param bondName
     * @param nics
     * @param address
     * @param subnet
     * @param gateway
     * @param bondingOptions
     * @param bootProtocol
     */
    public NetworkVdsmVDSCommandParameters(Guid vdsId,
            network net,
            String bondName,
            String[] nics,
            String address,
            String subnet,
            String gateway,
            String bondingOptions,
            NetworkBootProtocol bootProtocol) {
        this(vdsId, net.getname(), net.getvlan_id(), bondName, nics, address,
                subnet,
                gateway,
                net.getstp(),
                bondingOptions,
                bootProtocol);
        this.setVmNetwork(net.isVmNetwork());
        this.setNetwork(net);
    }

    public NetworkVdsmVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, networkName=%s, oldNetworkName=%s, hostAddr=%s, checkConnectivity=%s, " +
                "connectionTimeout=%s, vlanId=%s, bondName=%s, nics=%s, inetAddr=%s, networkMask=%s, gateway=%s, " +
                "stp=%s, bondingOptions=%s, bootProtocol=%s, vmNetwork=%s",
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
                getBootProtocol(),
                isVmNetwork());
    }

    public boolean isVmNetwork() {
        return vmNetwork;
    }

    public void setVmNetwork(boolean vmNetwork) {
        this.vmNetwork = vmNetwork;
    }

    public network getNetwork() {
        return network;
    }

    public void setNetwork(network network) {
        this.network = network;
    }
}
