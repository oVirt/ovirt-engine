package org.ovirt.engine.core.common.businessentities.network;

import java.util.Objects;

import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>NetworkInterface</code> represents a network interface device.
 *
 */
public abstract class NetworkInterface<T extends NetworkStatistics>
        implements Queryable, BusinessEntity<Guid>, Nameable {
    private static final long serialVersionUID = -4926026587466645571L;

    protected Guid id;

    @Size(min = 1, max = BusinessEntitiesDefinitions.NETWORK_INTERFACE_NAME_SIZE)
    private String name;

    private String macAddress;

    private Integer type;

    private Integer speed;

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
    @Override
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
        this.macAddress = macAddress == null ? null : macAddress.toLowerCase();
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
     * Returns whether the network device has non-zero speed.
     *
     * @return true iff the speed is set and is non-zero.
     */
    public boolean hasSpeed() {
        return speed != null && speed != 0;
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

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                macAddress,
                name,
                speed,
                type
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NetworkInterface)) {
            return false;
        }
        NetworkInterface<?> other = (NetworkInterface<?>) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(macAddress, other.macAddress)
                && Objects.equals(name, other.name)
                && Objects.equals(speed, other.speed)
                && Objects.equals(type, other.type);
    }
}
