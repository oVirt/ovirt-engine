package org.ovirt.engine.core.common.businessentities.network;

import java.util.Objects;

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

    private Long receivedBytes;

    private Long receivedBytesOffset;

    private Double transmitDropRate;

    private Double transmitRate;

    private Long transmittedBytes;

    private Long transmittedBytesOffset;

    private Double sampleTime;

    public NetworkStatistics() {
    }

    public NetworkStatistics(NetworkStatistics statistics) {
        setId(statistics.getId());
        setReceiveDropRate(statistics.getReceiveDropRate());
        setReceiveRate(statistics.getReceiveRate());
        setReceivedBytes(statistics.getReceivedBytes());
        setReceivedBytesOffset(statistics.getReceivedBytesOffset());
        setTransmitDropRate(statistics.getTransmitDropRate());
        setTransmitRate(statistics.getTransmitRate());
        setTransmittedBytes(statistics.getTransmittedBytes());
        setTransmittedBytesOffset(statistics.getTransmittedBytesOffset());
        setStatus(statistics.getStatus());
        setSampleTime(statistics.getSampleTime());
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
     * Sets the total received bytes.
     *
     * @param receivedBytes
     *            the total received bytes.
     */
    public void setReceivedBytes(Long receivedBytes) {
        this.receivedBytes = receivedBytes;
    }

    /**
     * Returns the total received bytes.
     *
     * @return the total received bytes.
     */
    public Long getReceivedBytes() {
        return receivedBytes;
    }

    /**
     * Sets the value by which total RX statistics should be offset (due to counter reset).
     *
     * @param receivedBytesOffset
     *            the RX byte offset.
     */
    public void setReceivedBytesOffset(Long receivedBytesOffset) {
        this.receivedBytesOffset = receivedBytesOffset;
    }

    /**
     * Gets the value by which total RX statistics should be offset (due to counter reset).
     *
     * @return the RX byte offset.
     */
    public Long getReceivedBytesOffset() {
        return receivedBytesOffset;
    }

    /**
     * Sets the value by which total TX statistics should be offset (due to counter reset).
     *
     * @param transmittedBytesOffset
     *            the TX byte offset.
     */
    public void setTransmittedBytesOffset(Long transmittedBytesOffset) {
        this.transmittedBytesOffset = transmittedBytesOffset;
    }

    /**
     * Gets the value by which total TX statistics should be offset (due to counter reset).
     *
     * @return the TX byte offset.
     */
    public Long getTransmittedBytesOffset() {
        return transmittedBytesOffset;
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

    /**
     * Sets the total transmitted bytes.
     *
     * @param transmittedBytes
     *            the total transmitted bytes.
     */
    public void setTransmittedBytes(Long transmittedBytes) {
        this.transmittedBytes = transmittedBytes;
    }

    /**
     * Returns the total transmitted bytes.
     *
     * @return the total transmitted bytes.
     */
    public Long getTransmittedBytes() {
        return transmittedBytes;
    }

    /**
     * Sets the time, in seconds, of the current statistics sample.
     *
     * @param sampleTime
     *            the current sample time in seconds.
     */
    public void setSampleTime(Double sampleTime) {
        this.sampleTime = sampleTime;
    }

    /**
     * Returns the time, in seconds, of the current statistics sample.
     *
     * @return the current sample time in seconds.
     */
    public Double getSampleTime() {
        return sampleTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                receiveDropRate,
                receiveRate,
                receivedBytes,
                receivedBytesOffset,
                status,
                transmitDropRate,
                transmitRate,
                transmittedBytes,
                transmittedBytesOffset,
                sampleTime
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NetworkStatistics)) {
            return false;
        }
        NetworkStatistics other = (NetworkStatistics) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(receiveDropRate, other.receiveDropRate)
                && Objects.equals(receiveRate, other.receiveRate)
                && Objects.equals(receivedBytes, other.receivedBytes)
                && Objects.equals(receivedBytesOffset, other.receivedBytesOffset)
                && Objects.equals(status, other.status)
                && Objects.equals(transmitDropRate, other.transmitDropRate)
                && Objects.equals(transmitRate, other.transmitRate)
                && Objects.equals(transmittedBytes, other.transmittedBytes)
                && Objects.equals(transmittedBytesOffset, other.transmittedBytesOffset)
                && Objects.equals(sampleTime, other.sampleTime);
    }
}
