package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class NetworkVdsmVDSCommandParameters extends VdsIdVDSCommandParametersBase {

    private String networkName;
    private String oldNetworkName;
    private String hostAddr;
    private boolean checkConnectivity;
    private int connectionTimeout;
    private Integer vlanId;
    private String bondName;
    private String[] nics;
    private String inetAddr;
    private String networkMask;
    private String gateway;
    private boolean stp;
    private String bondingOptions;
    private NetworkBootProtocol bootProtocol;
    private boolean vmNetwork;
    private Network network;

    public NetworkVdsmVDSCommandParameters() {
        bootProtocol = NetworkBootProtocol.NONE;
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
     *
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
            Network net,
            String bondName,
            String[] nics,
            String address,
            String subnet,
            String gateway,
            String bondingOptions,
            NetworkBootProtocol bootProtocol) {
        this(vdsId, net.getName(), net.getVlanId(), bondName, nics, address,
                subnet,
                gateway,
                net.getStp(),
                bondingOptions,
                bootProtocol);
        this.setVmNetwork(net.isVmNetwork());
        this.setNetwork(net);
    }

    public String getNetworkName() {
        return networkName;
    }

    private void setNetworkName(String value) {
        networkName = value;
    }

    public String getOldNetworkName() {
        return oldNetworkName;
    }

    public void setOldNetworkName(String value) {
        oldNetworkName = value;
    }

    public String getHostAddr() {
        return hostAddr;
    }

    public void setHostAddr(String value) {
        hostAddr = value;
    }

    public boolean getCheckConnectivity() {
        return checkConnectivity;
    }

    public void setCheckConnectivity(boolean value) {
        checkConnectivity = value;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int value) {
        connectionTimeout = value;
    }

    public Integer getVlanId() {
        return vlanId;
    }

    private void setVlanId(Integer value) {
        vlanId = value;
    }

    public String getBondName() {
        return bondName;
    }

    private void setBondName(String value) {
        bondName = value;
    }

    public String[] getNics() {
        return nics == null ? new String[0] : nics;
    }

    private void setNics(String[] value) {
        nics = value;
    }

    public String getInetAddr() {
        return inetAddr;
    }

    private void setInetAddr(String value) {
        inetAddr = value;
    }

    public String getNetworkMask() {
        return networkMask;
    }

    private void setNetworkMask(String value) {
        networkMask = value;
    }

    public String getGateway() {
        return gateway;
    }

    private void setGateway(String value) {
        gateway = value;
    }

    public boolean getStp() {
        return stp;
    }

    private void setStp(boolean value) {
        stp = value;
    }

    public String getBondingOptions() {
        return bondingOptions;
    }

    private void setBondingOptions(String value) {
        bondingOptions = value;
    }

    public NetworkBootProtocol getBootProtocol() {
        return bootProtocol;
    }

    private void setBootProtocol(NetworkBootProtocol value) {
        bootProtocol = value;
    }

    public boolean isVmNetwork() {
        return vmNetwork;
    }

    public void setVmNetwork(boolean vmNetwork) {
        this.vmNetwork = vmNetwork;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("networkName", getNetworkName())
                .append("oldNetworkName", getOldNetworkName())
                .append("hostAddr", getHostAddr())
                .append("checkConnectivity", getCheckConnectivity())
                .append("connectionTimeout", getConnectionTimeout())
                .append("vlanId", getVlanId())
                .append("bondName", getBondName())
                .append("nics", getNics())
                .append("inetAddr", getInetAddr())
                .append("networkMask", getNetworkMask())
                .append("gateway", getGateway())
                .append("stp", getStp())
                .append("bondingOptions", getBondingOptions())
                .append("bootProtocol", getBootProtocol())
                .append("vmNetwork", isVmNetwork())
                .append("network", getNetwork());
    }
}
