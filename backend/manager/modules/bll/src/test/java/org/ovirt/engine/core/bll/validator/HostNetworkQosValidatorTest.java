package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.RandomUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HostNetworkQosValidatorTest {

    private static int LOW_BANDWIDTH = 500;
    private static int HIGH_BANDWIDTH = 2000;

    @Mock
    private HostNetworkQos qos;

    private HostNetworkQosValidator validator;
    private HostNetworkQosValidator nullValidator;

    @BeforeEach
    public void setup() {
        validator = new HostNetworkQosValidator(qos);
        nullValidator = new HostNetworkQosValidator(null);
    }

    private void mockQos(Integer linkshare, Integer upperlimit, Integer realtime) {
        when(qos.getOutAverageLinkshare()).thenReturn(linkshare);
        when(qos.getOutAverageUpperlimit()).thenReturn(upperlimit);
        when(qos.getOutAverageRealtime()).thenReturn(realtime);
    }

    private int generateValue() {
        return RandomUtils.instance().nextInt(0, 1000000);
    }

    private void requiredValuesTest(Matcher<ValidationResult> matcher) {
        assertThat(validator.requiredValuesPresent(), matcher);
    }

    @Test
    public void onlyLinksharePresent() {
        mockQos(generateValue(), null, null);
        requiredValuesTest(isValid());
    }

    @Test
    public void allValuesPresent() {
        mockQos(generateValue(), generateValue(), generateValue());
        requiredValuesTest(isValid());
    }

    @Test
    public void noValuesPresent() {
        mockQos(null, null, null);
        requiredValuesTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_MISSING_VALUES));
    }

    @Test
    public void allButLinksharePresent() {
        mockQos(null, generateValue(), generateValue());
        requiredValuesTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_MISSING_VALUES));
    }

    @Test
    public void nullQosValuesPresent() {
        assertThat(nullValidator.requiredValuesPresent(), isValid());
    }

    private void consistentValuesTest(Matcher<ValidationResult> matcher) {
        assertThat(validator.valuesConsistent(), matcher);
    }

    @Test
    public void valuesConsistent() {
        mockQos(generateValue(), HIGH_BANDWIDTH, LOW_BANDWIDTH);
        consistentValuesTest(isValid());
    }

    @Test
    public void valuesConsistentEqual() {
        mockQos(generateValue(), HIGH_BANDWIDTH, HIGH_BANDWIDTH);
        consistentValuesTest(isValid());
    }

    @Test
    public void valuesConsistentOnlyLinkshare() {
        mockQos(generateValue(), null, null);
        consistentValuesTest(isValid());
    }

    @Test
    public void upperlimitLowerThanRealTime() {
        mockQos(generateValue(), LOW_BANDWIDTH, HIGH_BANDWIDTH);
        consistentValuesTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_INCONSISTENT_VALUES));
    }

    @Test
    public void nullQosValuesConsistent() {
        assertThat(nullValidator.valuesConsistent(), isValid());
    }

}
