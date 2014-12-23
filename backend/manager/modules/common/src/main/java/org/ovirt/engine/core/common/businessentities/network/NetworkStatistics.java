package org.ovirt.engine.core.common.businessentities.network;

import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>NetworkStatistics</code>
 *
 */
public abstract class NetworkStatistics implements BusinessEntityWithStatus<Guid, InterfaceStatus> {
    private static final long serialVersionUID = -748737255583275169L;

    private Guid id;

    private InterfaceStatus status;

    private Double receiveDropRate;

    private Double receiveRate;

    private Double transmitDropRate;

    private Double transmitRate;

    public NetworkStatistics() {
    }

    public NetworkStatistics(NetworkStatistics statistics) {
        setId(statistics.getId());
        setReceiveDropRate(statistics.getReceiveDropRate());
        setReceiveRate(statistics.getReceiveRate());
        setTransmitDropRate(statistics.getTransmitDropRate());
        setTransmitRate(statistics.getTransmitRate());
        setStatus(statistics.getStatus());
    }

    /**
     * Sets the instance id.
     *
     * @param id
     *            the id
     */
    public void setId(Guid id) {
        this.id = id;
    }

    /**
     * Returns the instance id.
     *
     * @return the id.
     */
    public Guid getId() {
        return id;
    }

    /**
     * Sets the status for the connection.
     *
     * @param status
     *            the status
     */
    @Override
    public void setStatus(InterfaceStatus status) {
        this.status = status;
    }

    /**
     * Returns the connection status.
     *
     * @return the status
     */
    @Override
    public InterfaceStatus getStatus() {
        return status;
    }

    /**
     * Sets the received data drop rate.
     *
     * @param receiveDropRate
     *            the rate
     */
    public void setReceiveDropRate(Double receiveDropRate) {
        this.receiveDropRate = receiveDropRate;
    }

    /**
     * Returns the received data drop rate.
     *
     * @return the rate
     */
    public Double getReceiveDropRate() {
        return receiveDropRate;
    }

    /**
     * Sets the data receive rate.
     *
     * @param receiveRate
     *            the rate
     */
    public void setReceiveRate(Double receiveRate) {
        this.receiveRate = receiveRate;
    }

    /**
     * Returns the data receive rate.
     *
     * @return the rate
     */
    public Double getReceiveRate() {
        return receiveRate;
    }

    /**
     * Sets the transmitted data drop rate.
     *
     * @param transmitDropRate
     *            the rate
     */
    public void setTransmitDropRate(Double transmitDropRate) {
        this.transmitDropRate = transmitDropRate;
    }

    /**
     * Returns the transmitted data drop rate.
     *
     * @return the rate
     */
    public Double getTransmitDropRate() {
        return transmitDropRate;
    }

    /**
     * Sets the data transmit rate.
     *
     * @param transmitRate
     *            the rate
     */
    public void setTransmitRate(Double transmitRate) {
        this.transmitRate = transmitRate;
    }

    /**
     * Returns the data transmit rate.
     *
     * @return the rate
     */
    public Double getTransmitRate() {
        return transmitRate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getReceiveDropRate() == null) ? 0 : getReceiveDropRate().hashCode());
        result = prime * result + ((getReceiveRate() == null) ? 0 : getReceiveRate().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getTransmitDropRate() == null) ? 0 : getTransmitDropRate().hashCode());
        result = prime * result + ((getTransmitRate() == null) ? 0 : getTransmitRate().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof NetworkStatistics))
            return false;
        NetworkStatistics other = (NetworkStatistics) obj;
        if (getId() == null) {
            if (other.getId() != null)
                return false;
        } else if (!getId().equals(other.getId()))
            return false;
        if (getReceiveDropRate() == null) {
            if (other.getReceiveDropRate() != null)
                return false;
        } else if (!getReceiveDropRate().equals(other.getReceiveDropRate()))
            return false;
        if (getReceiveRate() == null) {
            if (other.getReceiveRate() != null)
                return false;
        } else if (!getReceiveRate().equals(other.getReceiveRate()))
            return false;
        if (getStatus() != other.getStatus())
            return false;
        if (getTransmitDropRate() == null) {
            if (other.getTransmitDropRate() != null)
                return false;
        } else if (!getTransmitDropRate().equals(other.getTransmitDropRate()))
            return false;
        if (getTransmitRate() == null) {
            if (other.getTransmitRate() != null)
                return false;
        } else if (!getTransmitRate().equals(other.getTransmitRate()))
            return false;
        return true;
    }
}
