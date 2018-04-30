package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public class NetworkQosValidatorTest {

    private static final Guid DEFAULT_GUID = Guid.newGuid();
    private static final Guid OTHER_GUID = Guid.newGuid();
    private static final Integer BANDWIDTH_LOW = 10;
    private static final Integer BANDWIDTH_MEDIUM = 100;
    private static final Integer BANDWIDTH_HIGH = 1000;

    private NetworkQoS qos;
    private NetworkQoS oldQos;
    private List<NetworkQoS> allQos;
    private NetworkQosValidator validator;
    private NetworkQosValidator nullValidator;

    @BeforeEach
    public void setup() {
        qos = new NetworkQoS();
        oldQos = new NetworkQoS();
        allQos = new ArrayList<>();

        validator = spy(new NetworkQosValidator(qos));
        doReturn(oldQos).when(validator).getOldQos();
        doReturn(allQos).when(validator).getAllQosInDcByType();

        nullValidator = spy(new NetworkQosValidator(null));
        doReturn(oldQos).when(nullValidator).getOldQos();
    }

    private void qosExistsTest(Matcher<ValidationResult> matcher, NetworkQosValidator validator) {
        assertThat(validator.qosExists(), matcher);
    }

    @Test
    public void qosExistsNullInput() {
        oldQos.setId(DEFAULT_GUID);
        qosExistsTest(isValid(), nullValidator);
    }

    @Test
    public void qosDoesNotExistNullInput() {
        doReturn(null).when(nullValidator).getOldQos();
        qosExistsTest(isValid(), nullValidator);
    }

    @Test
    public void qosExists() {
        qos.setId(DEFAULT_GUID);
        oldQos.setId(DEFAULT_GUID);
        qosExistsTest(isValid(), validator);
    }

    @Test
    public void qosDoesNotExist() {
        qos.setId(DEFAULT_GUID);
        doReturn(null).when(validator).getOldQos();
        qosExistsTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_QOS_NOT_FOUND), validator);
    }

    private void consistentDataCenterTest(Matcher<ValidationResult> matcher) {
        assertThat(validator.consistentDataCenter(), matcher);
    }

    @Test
    public void sameDataCenter() {
        qos.setStoragePoolId(DEFAULT_GUID);
        oldQos.setStoragePoolId(DEFAULT_GUID);
        consistentDataCenterTest(isValid());
    }

    @Test
    public void differentDataCenter() {
        qos.setStoragePoolId(DEFAULT_GUID);
        oldQos.setStoragePoolId(OTHER_GUID);
        consistentDataCenterTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_QOS_STORAGE_POOL_NOT_CONSISTENT));
    }

    private void nameNotChangedOrNotTakenTest(Matcher<ValidationResult> matcher) {
        NetworkQoS otherQos = new NetworkQoS();
        allQos.add(otherQos);
        allQos.add(oldQos);
        otherQos.setName("foo");
        oldQos.setName("bar");

        assertThat(validator.nameNotChangedOrNotTaken(), matcher);
    }

    @Test
    public void nameNotChanged() {
        qos.setName("bar");
        nameNotChangedOrNotTakenTest(isValid());
    }

    @Test
    public void nameNotTaken() {
        qos.setName("fubar");
        nameNotChangedOrNotTakenTest(isValid());
    }

    @Test
    public void nameTaken() {
        qos.setName("foo");
        nameNotChangedOrNotTakenTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_QOS_NAME_EXIST));
    }

    private void valuesPresentTest(Matcher<ValidationResult> matcher) {
        assertThat(validator.requiredValuesPresent(), matcher);
    }

    @Test
    public void valuesPresentNullInput() {
        assertThat(nullValidator.requiredValuesPresent(), isValid());
    }

    @Test
    public void noValuePresent() {
        valuesPresentTest(isValid());
    }

    @Test
    public void allValuesPresent() {
        qos.setInboundAverage(BANDWIDTH_MEDIUM);
        qos.setInboundPeak(BANDWIDTH_MEDIUM);
        qos.setInboundBurst(BANDWIDTH_MEDIUM);
        qos.setOutboundAverage(BANDWIDTH_MEDIUM);
        qos.setOutboundPeak(BANDWIDTH_MEDIUM);
        qos.setOutboundBurst(BANDWIDTH_MEDIUM);
        valuesPresentTest(isValid());
    }

    @Test
    public void allInboundValuesPresent() {
        qos.setInboundAverage(BANDWIDTH_MEDIUM);
        qos.setInboundPeak(BANDWIDTH_MEDIUM);
        qos.setInboundBurst(BANDWIDTH_MEDIUM);
        valuesPresentTest(isValid());
    }

    @Test
    public void allOutboundValuesPresent() {
        qos.setOutboundAverage(BANDWIDTH_MEDIUM);
        qos.setOutboundPeak(BANDWIDTH_MEDIUM);
        qos.setOutboundBurst(BANDWIDTH_MEDIUM);
        valuesPresentTest(isValid());
    }

    @Test
    public void onlyInboundAveragePresent() {
        qos.setInboundAverage(BANDWIDTH_MEDIUM);
        valuesPresentTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_QOS_MISSING_VALUES));
    }

    @Test
    public void onlyInboundPeakPresent() {
        qos.setInboundPeak(BANDWIDTH_MEDIUM);
        valuesPresentTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_QOS_MISSING_VALUES));
    }

    @Test
    public void onlyInboundBurstPresent() {
        qos.setInboundBurst(BANDWIDTH_MEDIUM);
        valuesPresentTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_QOS_MISSING_VALUES));
    }

    @Test
    public void onlyOutboundAveragePresent() {
        qos.setOutboundAverage(BANDWIDTH_MEDIUM);
        valuesPresentTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_QOS_MISSING_VALUES));
    }

    @Test
    public void onlyOutboundPeakPresent() {
        qos.setOutboundPeak(BANDWIDTH_MEDIUM);
        valuesPresentTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_QOS_MISSING_VALUES));
    }

    @Test
    public void onlyOutboundBurstPresent() {
        qos.setOutboundBurst(BANDWIDTH_MEDIUM);
        valuesPresentTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_QOS_MISSING_VALUES));
    }

    @Test
    public void inboundBurstMissing() {
        qos.setInboundAverage(BANDWIDTH_MEDIUM);
        qos.setInboundPeak(BANDWIDTH_MEDIUM);
        valuesPresentTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_QOS_MISSING_VALUES));
    }

    @Test
    public void inboundPeakMissing() {
        qos.setInboundBurst(BANDWIDTH_MEDIUM);
        qos.setInboundAverage(BANDWIDTH_MEDIUM);
        valuesPresentTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_QOS_MISSING_VALUES));
    }

    @Test
    public void inboundAverageMissing() {
        qos.setInboundPeak(BANDWIDTH_MEDIUM);
        qos.setInboundBurst(BANDWIDTH_MEDIUM);
        valuesPresentTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_QOS_MISSING_VALUES));
    }

    @Test
    public void outboundBurstMissing() {
        qos.setOutboundAverage(BANDWIDTH_MEDIUM);
        qos.setOutboundPeak(BANDWIDTH_MEDIUM);
        valuesPresentTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_QOS_MISSING_VALUES));
    }

    @Test
    public void outboundPeakMissing() {
        qos.setOutboundBurst(BANDWIDTH_MEDIUM);
        qos.setOutboundAverage(BANDWIDTH_MEDIUM);
        valuesPresentTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_QOS_MISSING_VALUES));
    }

    @Test
    public void outboundAverageMissing() {
        qos.setOutboundPeak(BANDWIDTH_MEDIUM);
        qos.setOutboundBurst(BANDWIDTH_MEDIUM);
        valuesPresentTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_QOS_MISSING_VALUES));
    }

    private void peakConsistentWithAverageTest(Matcher<ValidationResult> matcher) {
        qos.setInboundAverage(BANDWIDTH_MEDIUM);
        qos.setOutboundAverage(BANDWIDTH_MEDIUM);
        assertThat(validator.peakConsistentWithAverage(), matcher);
    }

    @Test
    public void peakConsistentWithAverageNullInput() {
        assertThat(nullValidator.peakConsistentWithAverage(), isValid());
    }

    @Test
    public void peakHigherThanAverage() {
        qos.setInboundPeak(BANDWIDTH_HIGH);
        qos.setOutboundPeak(BANDWIDTH_HIGH);
        peakConsistentWithAverageTest(isValid());
    }

    @Test
    public void peakEqualToAverage() {
        qos.setInboundPeak(BANDWIDTH_MEDIUM);
        qos.setOutboundPeak(BANDWIDTH_MEDIUM);
        peakConsistentWithAverageTest(isValid());
    }

    @Test
    public void inboundPeakLowerThanAverage() {
        qos.setInboundPeak(BANDWIDTH_LOW);
        qos.setOutboundPeak(BANDWIDTH_HIGH);
        peakConsistentWithAverageTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_QOS_PEAK_LOWER_THAN_AVERAGE));
    }

    @Test
    public void outboundPeakLowerThanAverage() {
        qos.setInboundPeak(BANDWIDTH_HIGH);
        qos.setOutboundPeak(BANDWIDTH_LOW);
        peakConsistentWithAverageTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_QOS_PEAK_LOWER_THAN_AVERAGE));
    }
}
