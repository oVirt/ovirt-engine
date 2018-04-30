package org.ovirt.engine.core.bll.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.validation.groups.Default;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
public class QosRangeValidatorTest {

    public static final int OUT_OF_RANGE = 111;
    public static final int MAX_RANGE = 100;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.MaxCpuLimitQosValue, MAX_RANGE),
                MockConfigDescriptor.of(ConfigValues.MaxThroughputUpperBoundQosValue, MAX_RANGE),
                MockConfigDescriptor.of(ConfigValues.MaxReadThroughputUpperBoundQosValue, MAX_RANGE),
                MockConfigDescriptor.of(ConfigValues.MaxWriteThroughputUpperBoundQosValue, MAX_RANGE),
                MockConfigDescriptor.of(ConfigValues.MaxIopsUpperBoundQosValue, MAX_RANGE),
                MockConfigDescriptor.of(ConfigValues.MaxReadIopsUpperBoundQosValue, MAX_RANGE),
                MockConfigDescriptor.of(ConfigValues.MaxWriteIopsUpperBoundQosValue, MAX_RANGE),
                MockConfigDescriptor.of(ConfigValues.MaxAverageNetworkQoSValue, MAX_RANGE),
                MockConfigDescriptor.of(ConfigValues.MaxPeakNetworkQoSValue, MAX_RANGE),
                MockConfigDescriptor.of(ConfigValues.MaxBurstNetworkQoSValue, MAX_RANGE)
        );
    }

    @Test
    public void validCpuMessage() {
        CpuQos qos = new CpuQos();
        qos.setCpuLimit(OUT_OF_RANGE);

        List<String> validationMessages = ValidationUtils.validateInputs(
                Arrays.asList(new Class<?>[] { Default.class }),
                qos);

        assertValidateMsgCount(validationMessages, "$range 1-100", 1);
        assertValidateMsgCount(validationMessages, EngineMessage.ACTION_TYPE_FAILED_QOS_OUT_OF_RANGE_VALUES.name(), 1);
    }

    @Test
    public void validStorageMessage() {
        StorageQos qos = new StorageQos();
        qos.setMaxThroughput(OUT_OF_RANGE);
        qos.setMaxReadThroughput(OUT_OF_RANGE);
        qos.setMaxWriteThroughput(OUT_OF_RANGE);
        qos.setMaxIops(OUT_OF_RANGE);
        qos.setMaxReadIops(OUT_OF_RANGE);
        qos.setMaxWriteIops(OUT_OF_RANGE);

        List<String> validationMessages = ValidationUtils.validateInputs(
                Arrays.asList(new Class<?>[] { Default.class }),
                qos);

        assertValidateMsgCount(validationMessages, "$range 0-100", 6);
        assertValidateMsgCount(validationMessages, EngineMessage.ACTION_TYPE_FAILED_QOS_OUT_OF_RANGE_VALUES.name(), 6);
    }

    @Test
    public void validNetworkQosMessages() {
        NetworkQoS qos = new NetworkQoS();
        qos.setInboundAverage(OUT_OF_RANGE);
        qos.setInboundPeak(OUT_OF_RANGE);
        qos.setInboundBurst(OUT_OF_RANGE);
        qos.setOutboundAverage(OUT_OF_RANGE);
        qos.setOutboundPeak(OUT_OF_RANGE);
        qos.setOutboundBurst(OUT_OF_RANGE);

        List<String> validationMessages = ValidationUtils.validateInputs(
                Arrays.asList(new Class<?>[] { Default.class }),
                qos);

        assertValidateMsgCount(validationMessages, "$range 0-100", 6);
        assertValidateMsgCount(validationMessages, EngineMessage.ACTION_TYPE_FAILED_QOS_OUT_OF_RANGE_VALUES.name(), 6);
    }

    private void assertValidateMsgCount(List<String> validationMessages, String msg, int count) {
        assertEquals(Collections.frequency(validationMessages, msg), count);
    }

}
