package org.ovirt.engine.core.bll.scheduling;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.validation.groups.Default;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class QosRangeValidatorTest {

    public static final int OUT_OF_RANGE = 111;
    public static final int MAX_RANGE = 100;
    @Rule
    public final MockConfigRule configRule = new MockConfigRule();

    @Before
    public void init() {
        configRule.mockConfigValue(ConfigValues.MaxCpuLimitQosValue, MAX_RANGE);
        configRule.mockConfigValue(ConfigValues.MaxThroughputUpperBoundQosValue, MAX_RANGE);
        configRule.mockConfigValue(ConfigValues.MaxReadThroughputUpperBoundQosValue, MAX_RANGE);
        configRule.mockConfigValue(ConfigValues.MaxWriteThroughputUpperBoundQosValue, MAX_RANGE);
        configRule.mockConfigValue(ConfigValues.MaxIopsUpperBoundQosValue, MAX_RANGE);
        configRule.mockConfigValue(ConfigValues.MaxReadIopsUpperBoundQosValue, MAX_RANGE);
        configRule.mockConfigValue(ConfigValues.MaxWriteIopsUpperBoundQosValue, MAX_RANGE);
        configRule.mockConfigValue(ConfigValues.MaxAverageNetworkQoSValue, MAX_RANGE);
        configRule.mockConfigValue(ConfigValues.MaxPeakNetworkQoSValue, MAX_RANGE);
        configRule.mockConfigValue(ConfigValues.MaxBurstNetworkQoSValue, MAX_RANGE);
    }

    @Test
    public void validCpuMessage() {
        CpuQos qos = new CpuQos();
        qos.setCpuLimit(OUT_OF_RANGE);

        List<String> canDoMsgs = ValidationUtils.validateInputs(
                Arrays.asList(new Class<?>[] { Default.class }),
                qos);

        assertCanDoMsgCount(canDoMsgs, "$range 1-100", 1);
        assertCanDoMsgCount(canDoMsgs, EngineMessage.ACTION_TYPE_FAILED_QOS_OUT_OF_RANGE_VALUES.name(), 1);
    }

    private void assertCanDoMsgCount(List<String> canDoMsgs, String msg, int count) {
        assertTrue(Collections.frequency(canDoMsgs, msg) == count);
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

        List<String> canDoMsgs = ValidationUtils.validateInputs(
                Arrays.asList(new Class<?>[] { Default.class }),
                qos);

        assertCanDoMsgCount(canDoMsgs, "$range 0-100", 6);
        assertCanDoMsgCount(canDoMsgs, EngineMessage.ACTION_TYPE_FAILED_QOS_OUT_OF_RANGE_VALUES.name(), 6);
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

        List<String> canDoMsgs = ValidationUtils.validateInputs(
                Arrays.asList(new Class<?>[] { Default.class }),
                qos);

        assertCanDoMsgCount(canDoMsgs, "$range 0-100", 6);
        assertCanDoMsgCount(canDoMsgs, EngineMessage.ACTION_TYPE_FAILED_QOS_OUT_OF_RANGE_VALUES.name(), 6);
    }

}
