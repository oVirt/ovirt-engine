package org.ovirt.engine.ui.uicommonweb.models.hosts;

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

    private Double rxDrop;

    public Double getRxDrop() {
        return rxDrop;
    }

    public void setRxDrop(Double value) {
        rxDrop = value;
    }

    private Double txRate;

    public Double getTxRate() {
        return txRate;
    }

    public void setTxRate(Double value) {
        txRate = value;
    }

    private Long rxTotal;

    public Long getRxTotal() {
        return rxTotal;
    }

    public void setRxTotal(Long value) {
        rxTotal = value;
    }

    private Long txTotal;

    public Long getTxTotal() {
        return txTotal;
    }

    public void setTxTotal(Long value) {
        txTotal = value;
    }

    private Double txDrop;

    public Double getTxDrop() {
        return txDrop;
    }

    public void setTxDrop(Double value) {
        txDrop = value;
    }

}
