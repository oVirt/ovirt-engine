package org.ovirt.engine.core.common.businessentities.network;

import java.math.BigInteger;
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

    private BigInteger receiveDrops;

    private Double receiveRate;

    private BigInteger receivedBytes;

    private BigInteger receivedBytesOffset;

    private BigInteger transmitDrops;

    private Double transmitRate;

    private BigInteger transmittedBytes;

    private BigInteger transmittedBytesOffset;

    private Double sampleTime;

    public NetworkStatistics() {
    }

    public NetworkStatistics(NetworkStatistics statistics) {
        setId(statistics.getId());
        setReceiveDrops(statistics.getReceiveDrops());
        setReceiveRate(statistics.getReceiveRate());
        setReceivedBytes(statistics.getReceivedBytes());
        setReceivedBytesOffset(statistics.getReceivedBytesOffset());
        setTransmitDrops(statistics.getTransmitDrops());
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
     * Sets the received data drops.
     *
     * @param receiveDrops
     *            the rate
     */
    public void setReceiveDrops(BigInteger receiveDrops) {
        this.receiveDrops = receiveDrops;
    }

    /**
     * Returns the received data drops.
     *
     * @return the rate
     */
    public BigInteger getReceiveDrops() {
        return receiveDrops;
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
    public void setReceivedBytes(BigInteger receivedBytes) {
        this.receivedBytes = receivedBytes;
    }

    /**
     * Returns the total received bytes.
     *
     * @return the total received bytes.
     */
    public BigInteger getReceivedBytes() {
        return receivedBytes;
    }

    /**
     * Sets the value by which total RX statistics should be offset (due to counter reset).
     *
     * @param receivedBytesOffset
     *            the RX byte offset.
     */
    public void setReceivedBytesOffset(BigInteger receivedBytesOffset) {
        this.receivedBytesOffset = receivedBytesOffset;
    }

    /**
     * Gets the value by which total RX statistics should be offset (due to counter reset).
     *
     * @return the RX byte offset.
     */
    public BigInteger getReceivedBytesOffset() {
        return receivedBytesOffset;
    }

    /**
     * Sets the value by which total TX statistics should be offset (due to counter reset).
     *
     * @param transmittedBytesOffset
     *            the TX byte offset.
     */
    public void setTransmittedBytesOffset(BigInteger transmittedBytesOffset) {
        this.transmittedBytesOffset = transmittedBytesOffset;
    }

    /**
     * Gets the value by which total TX statistics should be offset (due to counter reset).
     *
     * @return the TX byte offset.
     */
    public BigInteger getTransmittedBytesOffset() {
        return transmittedBytesOffset;
    }

    /**
     * Sets the transmitted data drops.
     *
     * @param transmitDrops
     *            the rate
     */
    public void setTransmitDrops(BigInteger transmitDrops) {
        this.transmitDrops = transmitDrops;
    }

    /**
     * Returns the transmitted data drops.
     *
     * @return the rate
     */
    public BigInteger getTransmitDrops() {
        return transmitDrops;
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
    public void setTransmittedBytes(BigInteger transmittedBytes) {
        this.transmittedBytes = transmittedBytes;
    }

    /**
     * Returns the total transmitted bytes.
     *
     * @return the total transmitted bytes.
     */
    public BigInteger getTransmittedBytes() {
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

    public void resetVmStatistics() {
        setTransmitDrops(BigInteger.ZERO);
        setTransmitRate(0D);
        setReceiveRate(0D);
        setReceiveDrops(BigInteger.ZERO);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                receiveDrops,
                receiveRate,
                receivedBytes,
                receivedBytesOffset,
                status,
                transmitDrops,
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
                && Objects.equals(receiveDrops, other.receiveDrops)
                && Objects.equals(receiveRate, other.receiveRate)
                && Objects.equals(receivedBytes, other.receivedBytes)
                && Objects.equals(receivedBytesOffset, other.receivedBytesOffset)
                && Objects.equals(status, other.status)
                && Objects.equals(transmitDrops, other.transmitDrops)
                && Objects.equals(transmitRate, other.transmitRate)
                && Objects.equals(transmittedBytes, other.transmittedBytes)
                && Objects.equals(transmittedBytesOffset, other.transmittedBytesOffset)
                && Objects.equals(sampleTime, other.sampleTime);
    }
}
