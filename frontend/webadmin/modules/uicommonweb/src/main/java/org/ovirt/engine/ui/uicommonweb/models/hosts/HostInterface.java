package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.math.BigInteger;

import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class HostInterface extends Model {

    private VdsNetworkInterface privateInterface;

    public VdsNetworkInterface getInterface() {
        return privateInterface;
    }

    public void setInterface(VdsNetworkInterface value) {
        privateInterface = value;
    }

    private InterfaceStatus status = InterfaceStatus.values()[0];

    public InterfaceStatus getStatus() {
        return status;
    }

    public void setStatus(InterfaceStatus value) {
        status = value;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    private String ipv4Address;

    public String getIpv4Address() {
        return ipv4Address;
    }

    public void setIpv4Address(String value) {
        ipv4Address = value;
    }

    private String ipv6Address;

    public String getIpv6Address() {
        return ipv6Address;
    }

    public void setIpv6Address(String value) {
        ipv6Address = value;
    }

    private String mac;

    public String getMAC() {
        return mac;
    }

    public void setMAC(String value) {
        mac = value;
    }

    private Integer speed;

    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(Integer value) {
        speed = value;
    }

    private Double rxRate;

    public Double getRxRate() {
        return rxRate;
    }

    public void setRxRate(Double value) {
        rxRate = value;
    }

    private BigInteger rxDrop;

    public BigInteger getRxDrop() {
        return rxDrop;
    }

    public void setRxDrop(BigInteger value) {
        rxDrop = value;
    }

    private Double txRate;

    public Double getTxRate() {
        return txRate;
    }

    public void setTxRate(Double value) {
        txRate = value;
    }

    private BigInteger rxTotal;

    public BigInteger getRxTotal() {
        return rxTotal;
    }

    public void setRxTotal(BigInteger value) {
        rxTotal = value;
    }

    private BigInteger txTotal;

    public BigInteger getTxTotal() {
        return txTotal;
    }

    public void setTxTotal(BigInteger value) {
        txTotal = value;
    }

    private BigInteger txDrop;

    public BigInteger getTxDrop() {
        return txDrop;
    }

    public void setTxDrop(BigInteger value) {
        txDrop = value;
    }

}
