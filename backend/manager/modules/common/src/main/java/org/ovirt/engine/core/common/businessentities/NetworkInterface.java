package org.ovirt.engine.core.common.businessentities;

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

/**
 * <code>NetworkInterface</code> represents a network interface device.
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "NetworkInterface")
public abstract class NetworkInterface<T extends NetworkStatistics> extends IVdcQueryable
        implements BusinessEntity<Guid> {
    private static final long serialVersionUID = -4926026587466645571L;

    @XmlElement(name = "Id")
    protected Guid id;

    @Size(min = 1, max = BusinessEntitiesDefinitions.NETWORK_NAME_SIZE)
    @XmlElement(name = "Name")
    private String name;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAC_ADDR_SIZE)
    @XmlElement(name = "MacAddress")
    private String macAddress;

    @Size(max = BusinessEntitiesDefinitions.NETWORK_NAME_SIZE)
    @XmlElement(name = "NetworkName")
    private String networkName;

    @XmlElement(name = "Type", nillable = true)
    private Integer type;

    @XmlElement(name = "Speed", nillable = true)
    private Integer speed;

    @XmlElement(name = "Statistics")
    protected T statistics;

    public NetworkInterface() {
    }

    public NetworkInterface(T statistics, int type) {
        this.statistics = statistics;
        this.type = type;
    }

    /**
     * Sets the instance id.
     *
     * @param id
     *            the id
     */
    public void setId(Guid id) {
        this.id = id;
        this.statistics.setId(id);
    }

    /**
     * Returns the instance id.
     *
     * @return the id
     */
    public Guid getId() {
        return id;
    }

    /**
     * Sets the interface's name.
     *
     * @param name
     *            the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the interface's name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the MAC address.
     *
     * @param macAddress
     *            the MAC address
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * Returns the device's MAC address.
     *
     * @return the MAC address
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Sets the name of the network.
     *
     * @param networkName
     *            the network name
     */
    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    /**
     * Returns the name of the network.
     *
     * @return the network name
     */
    public String getNetworkName() {
        return networkName;
    }

    /**
     * Sets the speed of the network device in megabits per second.
     *
     * @param speed
     *            the speed.
     */
    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    /**
     * Returns the speed of the network device in megabits per second.
     *
     * @return the speed
     */
    public Integer getSpeed() {
        return speed;
    }

    /**
     * Sets the type of network device.
     *
     * @param type
     *            the type
     */
    public void setType(Integer type) {
        this.type = type != null ? type : 0;
    }

    /**
     * Returns the type of network device.
     *
     * @return the type
     */
    public Integer getType() {
        return type;
    }

    /**
     * Sets the statistics for the network device.
     *
     * @param statistics
     *            the statistics
     */
    public void setStatistics(T statistics) {
        this.statistics = statistics;
    }

    /**
     * Returns the statistics for the network device.
     *
     * @return the statistics
     */
    public T getStatistics() {
        return statistics;
    }
}
