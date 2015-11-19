package org.ovirt.engine.core.common.businessentities.qos;

import java.util.Objects;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.ConfiguredRange;

public class CpuQos extends QosBase {
    private static final long serialVersionUID = -5870254800787534586L;

    @ConfiguredRange(min = 1, maxConfigValue = ConfigValues.MaxCpuLimitQosValue,
            message = "ACTION_TYPE_FAILED_QOS_OUT_OF_RANGE_VALUES")
    private Integer cpuLimit;

    public CpuQos() {
        super(QosType.CPU);
    }

    public Integer getCpuLimit() {
        return cpuLimit;
    }

    public void setCpuLimit(Integer cpuLimit) {
        this.cpuLimit = cpuLimit;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CpuQos)) {
            return false;
        }
        CpuQos other = (CpuQos) obj;
        return super.equals(obj)
                && Objects.equals(cpuLimit, other.cpuLimit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                cpuLimit
        );
    }

    @Override
    public String getString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[")
                .append("cpu limit=")
                .append(cpuLimit)
                .append("]");
        return builder.toString();
    }

}
