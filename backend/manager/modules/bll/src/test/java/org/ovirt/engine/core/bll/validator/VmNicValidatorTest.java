package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.both;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.replacements;

import java.util.ArrayList;
import java.util.Arrays;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.network.NetworkQoSDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class VmNicValidatorTest {

    private static final Guid VNIC_PROFILE_ID = Guid.newGuid();
    private static final String CLUSTER_VERSION = "7";
    private static final ArrayList<String> NETWORK_DEVICES = new ArrayList<String>(
            Arrays.asList("rtl8139", "pv"));

    private static final String CLUSTER_VERSION_REPLACEMENT =
            String.format(VmNicValidator.CLUSTER_VERSION_REPLACEMENT_FORMAT, CLUSTER_VERSION);

    private final Guid DEFAULT_GUID = Guid.newGuid();
    private final Guid OTHER_GUID = Guid.newGuid();

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule();

    @Mock
    private VmNic nic;

    @Mock
    private Version version;

    private VmNicValidator validator;

    @Mock
    private DbFacade dbFacade;

    @Mock
    private VnicProfileDao vnicProfileDao;

    @Mock
    private NetworkQoSDao networkQosDao;

    @Mock
    private VnicProfile vnicProfile;

    @Mock
    private Network network;

    @Mock
    private NetworkQoS networkQos;

    @Before
    public void setup() {
        when(version.getValue()).thenReturn(CLUSTER_VERSION);
        validator = spy(new VmNicValidator(nic, version));
        doReturn(dbFacade).when(validator).getDbFacade();
    }

    @Test
    public void unlinkedWhenUnlinkingNotSupported() throws Exception {
        unlinkingTest(both(failsWith(VdcBllMessages.UNLINKING_IS_NOT_SUPPORTED))
                .and(replacements(hasItem(CLUSTER_VERSION_REPLACEMENT))),
                false,
                false);
    }

    @Test
    public void linkedWhenUnlinkingNotSupported() throws Exception {
        unlinkingTest(isValid(), false, true);
    }

    @Test
    public void unlinkedWhenUnlinkingSupported() throws Exception {
        unlinkingTest(isValid(), true, false);
    }

    @Test
    public void linkedWhenUnlinkingSupported() throws Exception {
        unlinkingTest(isValid(), true, true);
    }

    @Test
    public void nullVnicProfileWhenUnlinkingNotSupported() throws Exception {
        vnicProfileLinkingTest(both(failsWith(VdcBllMessages.NULL_NETWORK_IS_NOT_SUPPORTED))
                .and(replacements(hasItem(CLUSTER_VERSION_REPLACEMENT))),
                false,
                null);
    }

    @Test
    public void validVnicProfileWhenUnlinkingNotSupported() throws Exception {
        vnicProfileLinkingTest(isValid(), false, VNIC_PROFILE_ID);
    }

    @Test
    public void nullVnicProfileWhenUnlinkingSupported() throws Exception {
        vnicProfileLinkingTest(isValid(), true, null);
    }

    @Test
    public void validVnicProfileWhenUnlinkingSupported() throws Exception {
        vnicProfileLinkingTest(isValid(), true, VNIC_PROFILE_ID);
    }

    @Test
    public void e1000VmInterfaceTypeWhenNotIsCompatibleWithOs() throws Exception {
        isCompatibleWithOsTest(both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_VM_INTERFACE_TYPE_IS_NOT_SUPPORTED_BY_OS)),
                VmInterfaceType.e1000.getValue());
    }

    @Test
    public void pvVmInterfaceTypeWhenIsCompatibleWithOs() throws Exception {
        isCompatibleWithOsTest(isValid(), VmInterfaceType.pv.getValue());
    }

    @Test
    public void rtl8139VmInterfaceTypeWhenIsCompatibleWithOs() throws Exception {
        isCompatibleWithOsTest(isValid(), VmInterfaceType.rtl8139.getValue());
    }

    private void unlinkingTest(Matcher<ValidationResult> matcher, boolean networkLinkingSupported, boolean nicLinked) {
        mockConfigRule.mockConfigValue(ConfigValues.NetworkLinkingSupported, version, networkLinkingSupported);
        when(nic.isLinked()).thenReturn(nicLinked);

        assertThat(validator.linkedCorrectly(), matcher);
    }

    private void vnicProfileLinkingTest(Matcher<ValidationResult> matcher,
            boolean networkLinkingSupported,
            Guid vnicProfileId) {
        mockConfigRule.mockConfigValue(ConfigValues.NetworkLinkingSupported, version, networkLinkingSupported);
        when(nic.getVnicProfileId()).thenReturn(vnicProfileId);

        assertThat(validator.emptyNetworkValid(), matcher);
    }

    private void isCompatibleWithOsTest(Matcher<ValidationResult> matcher, int vmInterfaceType) {
        VmNicValidator validator = spy(new VmNicValidator(nic, version, 0));
        OsRepository osRepository = mock(OsRepository.class);
        when(validator.getOsRepository()).thenReturn(osRepository);
        when(osRepository.getNetworkDevices(any(Integer.class), any(Version.class))).thenReturn(NETWORK_DEVICES);
        when(nic.getType()).thenReturn(vmInterfaceType);

        assertThat(validator.isCompatibleWithOs(), matcher);
    }

    @Test
    public void vnicProfileExist() throws Exception {
        vnicProfileValidationTest(isValid(), true, true, false, false);
    }

    @Test
    public void vnicProfileNotExist() throws Exception {
        vnicProfileValidationTest(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_VNIC_PROFILE_NOT_EXISTS),
                false,
                false,
                false,
                false);
    }

    @Test
    public void networkQosSupported() throws Exception {
        vnicProfileValidationTest(isValid(), true, true, true, true);
    }

    @Test
    public void networkQosNotSupported() throws Exception {
        vnicProfileValidationTest(both(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_QOS_IS_NOT_SUPPORTED))
                .and(replacements(hasItem(CLUSTER_VERSION_REPLACEMENT))), true, true, true, false);
    }

    @Test
    public void networkQosNullAndSupported() throws Exception {
        vnicProfileValidationTest(isValid(), true, true, false, true);
    }

    @Test
    public void networkQosNullAndNotSupported() throws Exception {
        vnicProfileValidationTest(isValid(), true, true, false, false);
    }

    @Test
    public void networkInCluster() throws Exception {
        vnicProfileValidationTest(isValid(), true, true, false, false);
    }

    @Test
    public void networkNotInCluster() throws Exception {
        vnicProfileValidationTest(failsWith(VdcBllMessages.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER),
                true,
                false,
                false,
                false);
    }

    private void vnicProfileValidationTest(Matcher<ValidationResult> matcher,
            boolean profileExist,
            boolean networkExist,
            boolean qosNotNull,
            boolean qosSupported) {
        when(dbFacade.getVnicProfileDao()).thenReturn(vnicProfileDao);
        when(vnicProfileDao.get(any(Guid.class))).thenReturn(profileExist ? vnicProfile : null);
        when(vnicProfile.getNetworkId()).thenReturn(DEFAULT_GUID);

        doReturn(networkExist ? network : null).when(validator).getNetworkByVnicProfile(vnicProfile);
        doReturn(networkExist).when(validator).isNetworkInCluster(any(Network.class), any(Guid.class));

        mockConfigRule.mockConfigValue(ConfigValues.NetworkQosSupported, version, qosSupported);
        when(vnicProfile.getNetworkQosId()).thenReturn(qosNotNull ? DEFAULT_GUID : null);
        when(nic.getVnicProfileId()).thenReturn(VNIC_PROFILE_ID);

        assertThat(validator.profileValid(OTHER_GUID), matcher);
    }

}
