package org.ovirt.engine.core.common.businessentities.qos;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.ConfiguredRange;

public class StorageQos extends QosBase implements Serializable {

    public StorageQos() {
        super(QosType.STORAGE);
    }

    private static final long serialVersionUID = 1122123549710787758L;

    /* All Throughput values are in MiBs per second
     * Null and 0 values are interpreted as unlimited.
     */

    @ConfiguredRange(min = 0, maxConfigValue = ConfigValues.MaxThroughputUpperBoundQosValue,
            message = "ACTION_TYPE_FAILED_QOS_OUT_OF_RANGE_VALUES")
    private Integer maxThroughput;

    @ConfiguredRange(min = 0, maxConfigValue = ConfigValues.MaxReadThroughputUpperBoundQosValue,
            message = "ACTION_TYPE_FAILED_QOS_OUT_OF_RANGE_VALUES")
    private Integer maxReadThroughput;

    @ConfiguredRange(min = 0, maxConfigValue = ConfigValues.MaxWriteThroughputUpperBoundQosValue,
            message = "ACTION_TYPE_FAILED_QOS_OUT_OF_RANGE_VALUES")
    private Integer maxWriteThroughput;

    @ConfiguredRange(min = 0, maxConfigValue = ConfigValues.MaxIopsUpperBoundQosValue,
            message = "ACTION_TYPE_FAILED_QOS_OUT_OF_RANGE_VALUES")
    private Integer maxIops;

    @ConfiguredRange(min = 0, maxConfigValue = ConfigValues.MaxReadIopsUpperBoundQosValue,
            message = "ACTION_TYPE_FAILED_QOS_OUT_OF_RANGE_VALUES")
    private Integer maxReadIops;

    @ConfiguredRange(min = 0, maxConfigValue = ConfigValues.MaxWriteIopsUpperBoundQosValue,
            message = "ACTION_TYPE_FAILED_QOS_OUT_OF_RANGE_VALUES")
    private Integer maxWriteIops;

    public Integer getMaxThroughput() {
        return maxThroughput;
    }

    public void setMaxThroughput(Integer maxThroughput) {
        this.maxThroughput = maxThroughput;
    }

    public Integer getMaxReadThroughput() {
        return maxReadThroughput;
    }

    public void setMaxReadThroughput(Integer maxReadThroughput) {
        this.maxReadThroughput = maxReadThroughput;
    }

    public Integer getMaxWriteThroughput() {
        return maxWriteThroughput;
    }

    public void setMaxWriteThroughput(Integer maxWriteThroughput) {
        this.maxWriteThroughput = maxWriteThroughput;
    }

    public Integer getMaxIops() {
        return maxIops;
    }

    public void setMaxIops(Integer maxIops) {
        this.maxIops = maxIops;
    }

    public Integer getMaxReadIops() {
        return maxReadIops;
    }

    public void setMaxReadIops(Integer maxReadIops) {
        this.maxReadIops = maxReadIops;
    }

    public Integer getMaxWriteIops() {
        return maxWriteIops;
    }

    public void setMaxWriteIops(Integer maxWriteIops) {
        this.maxWriteIops = maxWriteIops;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StorageQos)) {
            return false;
        }
        StorageQos other = (StorageQos) obj;
        return super.equals(obj)
                && Objects.equals(maxThroughput, other.maxThroughput)
                && Objects.equals(maxReadThroughput, other.maxReadThroughput)
                && Objects.equals(maxWriteThroughput, other.maxWriteThroughput)
                && Objects.equals(maxIops, other.maxIops)
                && Objects.equals(maxReadIops, other.maxReadIops)
                && Objects.equals(maxWriteIops, other.maxWriteIops);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                maxIops,
                maxReadIops,
                maxReadThroughput,
                maxThroughput,
                maxWriteIops,
                maxWriteThroughput
        );
    }

    @Override
    public String getString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[")
                .append("Throughput")
                .append("{")
                .append("max=")
                .append(maxThroughput)
                .append("max read=")
                .append(maxReadThroughput)
                .append("max write=")
                .append(maxWriteThroughput)
                .append("}")
                .append("IOPS")
                .append("{")
                .append("max=")
                .append(maxIops)
                .append("max read=")
                .append(maxReadIops)
                .append("max write=")
                .append(maxWriteIops)
                .append("}")
                .append("]");
        return builder.toString();
    }

}
