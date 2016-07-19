package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.atLeastOnce;
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
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.network.NetworkQoSDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;

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

    private VM vm;

    @Before
    public void setup() {
        when(version.getValue()).thenReturn(CLUSTER_VERSION);
        validator = spy(new VmNicValidator(nic, version));
        doReturn(dbFacade).when(validator).getDbFacade();
        vm = new VM();
    }

    @Test
    public void unlinkedWhenUnlinkingNotSupported() throws Exception {
        unlinkingTest(both(failsWith(EngineMessage.UNLINKING_IS_NOT_SUPPORTED))
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
        vnicProfileLinkingTest(both(failsWith(EngineMessage.NULL_NETWORK_IS_NOT_SUPPORTED))
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
        isCompatibleWithOsTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_VM_INTERFACE_TYPE_IS_NOT_SUPPORTED_BY_OS),
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

        assertThat(validator.linkedOnlyIfSupported(), matcher);
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
        vnicProfileValidationTest(isValid(), true, true);
    }

    @Test
    public void vnicProfileNotExist() throws Exception {
        vnicProfileValidationTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_VNIC_PROFILE_NOT_EXISTS),
                false,
                false);
    }

    @Test
    public void networkInCluster() throws Exception {
        vnicProfileValidationTest(isValid(), true, true);
    }

    @Test
    public void networkNotInCluster() throws Exception {
        vnicProfileValidationTest(failsWith(EngineMessage.NETWORK_NOT_EXISTS_IN_CURRENT_CLUSTER),
                true,
                false);
    }

    private void vnicProfileValidationTest(Matcher<ValidationResult> matcher,
            boolean profileExists,
            boolean networkExists) {
        when(nic.getVnicProfileId()).thenReturn(VNIC_PROFILE_ID);
        doReturn(profileExists ? vnicProfile : null).when(validator).loadVnicProfile(VNIC_PROFILE_ID);
        doReturn(networkExists ? network : null).when(validator).getNetworkByVnicProfile(vnicProfile);
        doReturn(networkExists).when(validator).isNetworkInCluster(network, OTHER_GUID);

        assertThat(validator.profileValid(OTHER_GUID), matcher);

        verify(nic, atLeastOnce()).getVnicProfileId();
        verify(validator).loadVnicProfile(VNIC_PROFILE_ID);
        if (networkExists) {
            verify(validator).getNetworkByVnicProfile(vnicProfile);
            verify(validator).isNetworkInCluster(network, OTHER_GUID);
        }
    }

    @Test
    public void typeMatchesProfileBothNotPassthrough() {
        typeMatchesProfileCommon(false, false);
        assertThat(validator.typeMatchesProfile(), isValid());
    }

    @Test
    public void typeMatchesProfilePassthrough() {
        typeMatchesProfileCommon(true, true);
        assertThat(validator.typeMatchesProfile(), isValid());
    }

    @Test
    public void typeMatchesProfileOnlyTypePassthrough() {
        typeMatchesProfileCommon(true, false);
        assertThat(validator.typeMatchesProfile(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_VM_INTERFACE_TYPE_NOT_MATCH_PROFILE));
    }

    @Test
    public void typeMatchesProfileOnlyProfilePassthrough() {
        typeMatchesProfileCommon(false, true);
        assertThat(validator.typeMatchesProfile(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_VM_INTERFACE_TYPE_NOT_MATCH_PROFILE));
    }

    @Test
    public void typeMatchesProfileTypePassthroughProfileIsNull() {
        typeMatchesProfileCommon(true, null);
        assertThat(validator.typeMatchesProfile(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_VM_INTERFACE_TYPE_NOT_MATCH_PROFILE));
    }

    @Test
    public void typeMatchesProfileTypeNotPassthroughProfileIsNull() {
        typeMatchesProfileCommon(false, null);
        assertThat(validator.typeMatchesProfile(), isValid());
    }

    private void typeMatchesProfileCommon(boolean typePassthorugh, Boolean profilePassthorugh) {
        when(nic.getVnicProfileId()).thenReturn(VNIC_PROFILE_ID);
        doReturn(profilePassthorugh == null ? null : vnicProfile).when(validator).loadVnicProfile(VNIC_PROFILE_ID);

        if (profilePassthorugh != null) {
            when(vnicProfile.isPassthrough()).thenReturn(profilePassthorugh);
        }

        when(nic.getType()).thenReturn(typePassthorugh ? VmInterfaceType.pciPassthrough.getValue()
                : ((VmInterfaceType) anyEnumBut(VmInterfaceType.pciPassthrough)).getValue());
    }

    private <T extends Enum<?>> Enum<?> anyEnumBut(T excludeEnum) {
        Enum<?> returnEnum = excludeEnum;

        while (returnEnum == excludeEnum) {
            returnEnum = RandomUtils.instance().nextEnum(excludeEnum.getClass());
        }

        return returnEnum;
    }

    @Test
    public void passthroughVnicLinked() {
        passthroughIsLinkedCommon(true, true, isValid());
    }

    @Test
    public void passthroughVnicUnlinked() {
        passthroughIsLinkedCommon(true,
                false,
                failsWith(EngineMessage.ACTION_TYPE_FAILED_UNLINKING_OF_PASSTHROUGH_VNIC_IS_NOT_SUPPORTED));
    }

    @Test
    public void nonPassthroughVnicLinked() {
        passthroughIsLinkedCommon(false, true, isValid());
    }

    @Test
    public void nonPassthroughVnicUnLinked() {
        passthroughIsLinkedCommon(false, false, isValid());
    }

    private void passthroughIsLinkedCommon(boolean isPassthrough, boolean isLinked, Matcher<ValidationResult> matcher) {
        when(nic.isPassthrough()).thenReturn(isPassthrough);
        when(nic.isLinked()).thenReturn(isLinked);

        assertThat(validator.passthroughIsLinked(), matcher);
    }

    @Test
    public void forbidEmptyProfileForHostedEngineVm(){
        vm.setOrigin(OriginType.HOSTED_ENGINE);
        assertThat(validator.validateProfileNotEmptyForHostedEngineVm(vm),
                failsWith(EngineMessage.HOSTED_ENGINE_VM_CANNOT_HAVE_NIC_WITH_EMPTY_PROFILE));
    }

    @Test
    public void allowEmptyProfileForNonHostedEngineVm(){
        vm.setOrigin(OriginType.RHEV);
        assertThat(validator.validateProfileNotEmptyForHostedEngineVm(vm), isValid());
    }
}
