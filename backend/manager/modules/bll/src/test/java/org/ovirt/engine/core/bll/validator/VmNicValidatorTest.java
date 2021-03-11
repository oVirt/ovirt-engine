package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.atLeastOnce;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.ArrayList;
import java.util.Arrays;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.InjectorExtension;
import org.ovirt.engine.core.utils.RandomUtils;

@ExtendWith({MockitoExtension.class, InjectorExtension.class })
@MockitoSettings(strictness = Strictness.LENIENT)
public class VmNicValidatorTest {

    private static final Guid VNIC_PROFILE_ID = Guid.newGuid();
    private static final ArrayList<String> NETWORK_DEVICES = new ArrayList<>(
            Arrays.asList("rtl8139", "pv"));

    private final Guid OTHER_GUID = Guid.newGuid();

    @Mock
    private VmNic nic;

    @Mock
    private Version version;

    private VmNicValidator validator;

    @Mock
    private VnicProfile vnicProfile;

    @Mock
    private Network network;

    @Mock
    @InjectedMock
    public OsRepository osRepository;

    private VM vm;

    private Cluster cluster;

    @BeforeEach
    public void setup() {
        validator = spy(new VmNicValidator(nic, version));
        vm = new VM();
        cluster = new Cluster();
    }

    @Test
    public void e1000VmInterfaceTypeWhenNotIsCompatibleWithOs() {
        isCompatibleWithOsTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_VM_INTERFACE_TYPE_IS_NOT_SUPPORTED_BY_OS),
                VmInterfaceType.e1000.getValue());
    }

    @Test
    public void pvVmInterfaceTypeWhenIsCompatibleWithOs() {
        isCompatibleWithOsTest(isValid(), VmInterfaceType.pv.getValue());
    }

    @Test
    public void rtl8139VmInterfaceTypeWhenIsCompatibleWithOs() {
        isCompatibleWithOsTest(isValid(), VmInterfaceType.rtl8139.getValue());
    }

    private void isCompatibleWithOsTest(Matcher<ValidationResult> matcher, int vmInterfaceType) {
        VmNicValidator validator = spy(new VmNicValidator(nic, version, 0));
        when(osRepository.getNetworkDevices(anyInt(), any())).thenReturn(NETWORK_DEVICES);
        when(nic.getType()).thenReturn(vmInterfaceType);

        assertThat(validator.isCompatibleWithOs(), matcher);
    }

    @Test
    public void vnicProfileExist() {
        vnicProfileValidationTest(isValid(), true, true);
    }

    @Test
    public void vnicProfileNotExist() {
        vnicProfileValidationTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_VNIC_PROFILE_NOT_EXISTS),
                false,
                false);
    }

    @Test
    public void networkInCluster() {
        vnicProfileValidationTest(isValid(), true, true);
    }

    @Test
    public void networkNotInCluster() {
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

    @Test
    public void networkSupportedByClusterSwitchLegacyTypeSuccess() {
        ovsSwitchTypeValidationTest(isValid(),
                false,
                true,
                false);
    }

    @Test
    public void networkSupportedByClusterSwitchTypeNonExternalNetworkFail() {
        ovsSwitchTypeValidationTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_ONLY_EXTERNAL_NETWORK_IS_SUPPORTED_IN_OVS_SWITCH_TYPE),
                true,
                false,
                false);
    }

    @Test
    public void networkSupportedByClusterSwitchTypeSuccess() {
        ovsSwitchTypeValidationTest(isValid(),
                true,
                true,
                false);
    }

    @Test
    public void networkSupportedByClusterSwitchTypeEmptyNetworkSuccess() {
        ovsSwitchTypeValidationTest(isValid(),
                true,
                false,
                true);
    }

    private void ovsSwitchTypeValidationTest(Matcher<ValidationResult> matcher,
            boolean clusterSwitchOvs,
            boolean networkIsExternal,
            boolean nullNetwork) {
        cluster.setRequiredSwitchTypeForCluster(clusterSwitchOvs ? SwitchType.OVS : SwitchType.LEGACY);
        when(network.isExternal()).thenReturn(networkIsExternal);
        doReturn(nullNetwork ? null : network).when(validator).getNetwork();

        assertThat(validator.isNetworkSupportedByClusterSwitchType(cluster), matcher);
    }

    @Test
    public void withoutFailoverCluster45Success() {
        failoverClusterVersionTest(isValid(), false, Version.v4_5);
    }

    @Test
    public void withoutFailoverCluster46Success() {
        failoverClusterVersionTest(isValid(), false, Version.v4_6);
    }

    @Test
    public void withFailoverCluster45Fail() {
        failoverClusterVersionTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_VM_INTERFACE_WITH_FAILOVER_IS_SUPPORTED_ONLY_IN_CLUSTER_4_6_AND_ABOVE),
                true,
                Version.v4_5);
    }

    @Test
    public void withFailoverCluster46Success() {
        failoverClusterVersionTest(isValid(), true, Version.v4_6);
    }

    private void failoverClusterVersionTest(Matcher<ValidationResult> matcher,
            boolean hasFailover,
            Version clusterVersion) {
        validator.version = clusterVersion;
        doReturn(vnicProfile).when(validator).getVnicProfile();
        doReturn(hasFailover ? Guid.newGuid() : null).when(vnicProfile).getFailoverVnicProfileId();

        assertThat(validator.isFailoverInSupportedClusterVersion(), matcher);
    }
}
