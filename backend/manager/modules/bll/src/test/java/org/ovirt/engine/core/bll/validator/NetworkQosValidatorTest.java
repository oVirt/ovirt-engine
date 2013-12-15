package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

public class NetworkQosValidatorTest {

    private static final Guid DEFAULT_GUID = Guid.newGuid();
    private static final Guid OTHER_GUID = Guid.newGuid();

    private NetworkQoS qos;
    private NetworkQoS oldQos;
    private List<NetworkQoS> allQos;
    private NetworkQosValidator validator;
    private NetworkQosValidator nullValidator;

    @Before
    public void setup() {
        qos = new NetworkQoS();
        oldQos = new NetworkQoS();
        allQos = new ArrayList<NetworkQoS>();

        validator = spy(new NetworkQosValidator(qos));
        doReturn(oldQos).when(validator).getOldQos();
        doReturn(allQos).when(validator).getAllQosInDc();

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
        qosExistsTest(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_QOS_NOT_FOUND), validator);
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
        consistentDataCenterTest(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_QOS_INVALID_DC_ID));
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
    public void NameNotTaken() {
        qos.setName("fubar");
        nameNotChangedOrNotTakenTest(isValid());
    }

    @Test
    public void NameTaken() {
        qos.setName("foo");
        nameNotChangedOrNotTakenTest(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_QOS_NAME_EXIST));
    }
}
