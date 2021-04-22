package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.NetworkFilterDao;
import org.ovirt.engine.core.dao.network.NetworkQoSDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.InjectorExtension;
import org.ovirt.engine.core.utils.ReplacementUtils;

@ExtendWith({MockitoExtension.class, InjectorExtension.class })
@MockitoSettings(strictness = Strictness.LENIENT)
public class VnicProfileValidatorTest {

    private static final String NAMEABLE_NAME = "nameable";
    private static final String DEFAULT_VNIC_PROFILE_NAME = "myvnicprofile";
    private static final String OTHER_VNIC_PROFILE_NAME = "myothervnicprofile";
    private static final Guid DEFAULT_GUID = Guid.newGuid();
    private static final Guid OTHER_GUID = Guid.newGuid();
    private static final Guid VALID_NETWORK_FILTER_ID = Guid.newGuid();
    private static final Guid INVALID_NETWORK_FILTER_ID = Guid.newGuid();

    @Mock
    @InjectedMock
    public VnicProfileDao vnicProfileDao;

    @Mock
    @InjectedMock
    public NetworkDao networkDao;

    @Mock
    @InjectedMock
    public NetworkQoSDao networkQosDao;

    @Mock
    @InjectedMock
    public VmDao vmDao;

    @Mock
    @InjectedMock
    public NetworkFilterDao networkFilterDao;

    @Mock
    private VnicProfile vnicProfile;

    @Mock
    private Network network;

    @Mock
    private NetworkQoS networkQos;

    @Mock
    @InjectedMock
    public VmTemplateDao templateDao;

    private List<VnicProfile> vnicProfiles = new ArrayList<>();

    private VnicProfileValidator validator;

    @BeforeEach
    public void setup() {
        validator = new VnicProfileValidator(vnicProfile);

        // mock some commonly used Daos
        initNetworkFilterDao();

        // mock their getters
        when(vnicProfileDao.get(any())).thenReturn(vnicProfile);
        when(vnicProfileDao.getAllForNetwork(any())).thenReturn(vnicProfiles);
    }

    private void initNetworkFilterDao() {
        when(networkFilterDao.getNetworkFilterById(INVALID_NETWORK_FILTER_ID)).thenReturn(null);
        when(networkFilterDao.getNetworkFilterById(VALID_NETWORK_FILTER_ID))
                .thenReturn(new NetworkFilter(VALID_NETWORK_FILTER_ID));
    }

    @Test
    public void vnicProfileSet() {
        assertThat(validator.vnicProfileIsSet(), isValid());
    }

    @Test
    public void vnicProfileNull() {
        validator = new VnicProfileValidator(null);
        assertThat(validator.vnicProfileIsSet(), failsWith(EngineMessage.ACTION_TYPE_FAILED_VNIC_PROFILE_NOT_EXISTS));
    }

    @Test
    public void vnicProfileExists() {
        assertThat(validator.vnicProfileExists(), isValid());
    }

    @Test
    public void vnicProfileDoesNotExist() {
        when(vnicProfileDao.get(any())).thenReturn(null);
        assertThat(validator.vnicProfileExists(), failsWith(EngineMessage.ACTION_TYPE_FAILED_VNIC_PROFILE_NOT_EXISTS));
    }

    @Test
    public void networkExists() {
        when(networkDao.get(any())).thenReturn(network);
        assertThat(validator.networkExists(), isValid());
    }

    @Test
    public void networkDoesntExist() {
        when(networkDao.get(any())).thenReturn(null);
        assertThat(validator.networkExists(), failsWith(EngineMessage.NETWORK_HAVING_ID_NOT_EXISTS));
    }

    @Test
    public void networkQosExists() {
        when(vnicProfile.getNetworkQosId()).thenReturn(DEFAULT_GUID);
        when(networkQosDao.get(DEFAULT_GUID)).thenReturn(networkQos);
        assertThat(validator.networkQosExistsOrNull(), isValid());
    }

    @Test
    public void networkQosNull() {
        when(vnicProfile.getNetworkQosId()).thenReturn(null);
        assertThat(validator.networkQosExistsOrNull(), isValid());
    }

    @Test
    public void networkQosDoesntExist() {
        when(vnicProfile.getNetworkQosId()).thenReturn(DEFAULT_GUID);
        when(networkQosDao.get(any())).thenReturn(null);
        assertThat(validator.networkQosExistsOrNull(), failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_QOS_NOT_EXISTS));
    }

    private void vnicProfileAvailableTest(Matcher<ValidationResult> matcher, List<VnicProfile> vnicProfiles) {
        this.vnicProfiles.addAll(vnicProfiles);
        when(vnicProfile.getName()).thenReturn(DEFAULT_VNIC_PROFILE_NAME);
        when(vnicProfile.getId()).thenReturn(DEFAULT_GUID);

        assertThat(validator.vnicProfileNameNotUsed(), matcher);
    }

    private static List<VnicProfile> getSingletonNamedVnicProfileList(String vnicProfileName, Guid vnicProfileId) {
        VnicProfile vnicProfile = mock(VnicProfile.class);
        when(vnicProfile.getName()).thenReturn(vnicProfileName);
        when(vnicProfile.getId()).thenReturn(vnicProfileId);
        return Collections.singletonList(vnicProfile);
    }

    @Test
    public void vnicProfileNameNoVnicProfiles() {
        vnicProfileAvailableTest(isValid(), Collections.emptyList());
    }

    @Test
    public void vnicProfileNameAvailable() {
        vnicProfileAvailableTest(isValid(), getSingletonNamedVnicProfileList(OTHER_VNIC_PROFILE_NAME, OTHER_GUID));
    }

    @Test
    public void vnicProfileNameTakenByDifferentVnicProfile() {
        vnicProfileAvailableTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_VNIC_PROFILE_NAME_IN_USE),
                getSingletonNamedVnicProfileList(DEFAULT_VNIC_PROFILE_NAME, OTHER_GUID));
    }

    @Test
    public void vnicProfileNameTakenCaseSensitivelyByDifferentVnicProfile() {
        vnicProfileAvailableTest(isValid(),
                getSingletonNamedVnicProfileList(DEFAULT_VNIC_PROFILE_NAME.toUpperCase(), OTHER_GUID));
    }

    @Test
    public void vnicProfileNameTakenBySameVnicProfile() {
        vnicProfileAvailableTest(isValid(),
                getSingletonNamedVnicProfileList(DEFAULT_VNIC_PROFILE_NAME, DEFAULT_GUID));
    }

    private static Matcher<ValidationResult> failsWithVnicProfileInUse() {
        return failsWith(EngineMessage.ACTION_TYPE_FAILED_VNIC_PROFILE_IN_ONE_USE);
    }

    @Test
    public void networkChanged() {
        mockVnicProfileNetworkChange(DEFAULT_GUID, DEFAULT_GUID);
        assertThat(validator.networkNotChanged(), isValid());
    }

    @Test
    public void changingNetworkNotAllowed() {
        mockVnicProfileNetworkChange(DEFAULT_GUID, OTHER_GUID);
        assertThat(validator.networkNotChanged(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_CHANGE_VNIC_PROFILE_NETWORK));
    }

    @Test
    public void portMirroringNotChanged() {
        assertThat(validator.portMirroringNotChangedIfUsedByVms(), isValid());
    }

    @Test
    public void portMirroringEnableSupported() {
        mockVnicProfilePortMirroringChange(false);
        mockVmsUsingVnicProfile(Collections.emptyList());
        assertThat(validator.portMirroringNotChangedIfUsedByVms(), isValid());
    }

    @Test
    public void portMirroringChangeNotSupported() {
        mockVnicProfilePortMirroringChange(false);
        mockVmsUsingVnicProfile(Collections.singletonList(mock(VM.class)));
        assertThat(validator.portMirroringNotChangedIfUsedByVms(), failsWithVnicProfileInUse());
    }

    private void mockVnicProfileNetworkChange(Guid vnicProfileId, Guid oldVnicProfileId) {
        VnicProfile vnicProfile = mock(VnicProfile.class);
        when(this.vnicProfile.getNetworkId()).thenReturn(vnicProfileId);
        when(vnicProfile.getNetworkId()).thenReturn(oldVnicProfileId);
        when(vnicProfileDao.get(any())).thenReturn(vnicProfile);
    }

    private void vnicProfileNotUsedByVmsTest(Matcher<ValidationResult> matcher, List<VM> vms) {
        mockVmsUsingVnicProfile(vms);
        assertThat(validator.vnicProfileNotUsedByVms(), matcher);
    }

    private void mockVmsUsingVnicProfile(List<VM> vms) {
        when(vmDao.getAllForVnicProfile(any())).thenReturn(vms);
    }

    private void mockVnicProfilePortMirroringChange(boolean portMirroring) {
        VnicProfile vnicProfile = mock(VnicProfile.class);
        when(this.vnicProfile.isPortMirroring()).thenReturn(portMirroring);
        when(vnicProfile.isPortMirroring()).thenReturn(!portMirroring);
        when(vnicProfileDao.get(any())).thenReturn(vnicProfile);
    }

    @Test
    public void vnicProfileNotInUseByVms() {
        vnicProfileNotUsedByVmsTest(isValid(), Collections.emptyList());
    }

    @Test
    public void vnicProfileInUseByVms() {
        VM vm = mock(VM.class);
        when(vm.getName()).thenReturn(NAMEABLE_NAME);
        vnicProfileNotUsedByVmsTest(failsWithVnicProfileInUse(), Collections.singletonList(vm));
    }

    private void vnicProfileNotUsedByTemplatesTest(Matcher<ValidationResult> matcher, List<VmTemplate> templates) {
        when(templateDao.getAllForVnicProfile(any())).thenReturn(templates);
        assertThat(validator.vnicProfileNotUsedByTemplates(), matcher);
    }

    @Test
    public void vnicProfileNotInUseByTemplates() {
        vnicProfileNotUsedByTemplatesTest(isValid(), Collections.emptyList());
    }

    @Test
    public void vnicProfileInUseByTemplates() {
        VmTemplate template = mock(VmTemplate.class);
        when(template.getName()).thenReturn(NAMEABLE_NAME);

        vnicProfileNotUsedByTemplatesTest(failsWithVnicProfileInUse(), Collections.singletonList(template));
    }

    @Test
    public void vnicProfileForVmNetwork() {
        vnicProfileForVmNetworkTest(true, isValid());
    }

    @Test
    public void vnicProfileForNonVmNetwork() {
        vnicProfileForVmNetworkTest(false,
                failsWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_ADD_VNIC_PROFILE_TO_NON_VM_NETWORK));
    }

    private void vnicProfileForVmNetworkTest(boolean vmNetwork, Matcher<ValidationResult> matcher) {
        when(network.isVmNetwork()).thenReturn(vmNetwork);
        when(networkDao.get(any())).thenReturn(network);
        assertThat(validator.vnicProfileForVmNetworkOnly(), matcher);
    }

    @Test
    public void vnicProfileForNoPortIsolationNetwork() {
        vnicProfileForPortIsolationNetworkTest(false, isValid());
    }

    @Test
    public void vnicProfileForPortIsolationNetwork() {
        vnicProfileForPortIsolationNetworkTest(true,
                failsWith(EngineMessage.ACTION_TYPE_FAILED_PASSTHROUGH_PROFILE_NOT_SUPPORTS_PORT_ISOLATION));
    }

    private void vnicProfileForPortIsolationNetworkTest(boolean portIsolation, Matcher<ValidationResult> matcher) {
        when(network.isPortIsolation()).thenReturn(portIsolation);
        when(networkDao.get(any())).thenReturn(network);
        when(vnicProfile.isPassthrough()).thenReturn(true);
        assertThat(validator.passthroughProfileNoPortIsolation(), matcher);
    }

    @Test
    public void externalNetworkPortMirroring() {
        externalNetworkPortMirroringTest(true,
                true,
                failsWith(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_CANNOT_BE_PORT_MIRRORED));
    }

    @Test
    public void externalNetworkNotPortMirroring() {
        externalNetworkPortMirroringTest(true, false, isValid());
    }

    @Test
    public void internalNetworkPortMirroring() {
        externalNetworkPortMirroringTest(false, true, isValid());
    }

    @Test
    public void internalNetworkNotPortMirroring() {
        externalNetworkPortMirroringTest(false, false, isValid());
    }

    private void externalNetworkPortMirroringTest(boolean externalNetwork,
            boolean portMirroring,
            Matcher<ValidationResult> matcher) {
        when(networkDao.get(any())).thenReturn(network);
        when(network.isExternal()).thenReturn(externalNetwork);
        when(vnicProfile.isPortMirroring()).thenReturn(portMirroring);
        assertThat(validator.portMirroringNotSetIfExternalNetwork(), matcher);
    }

    @Test
    public void passthroughChangedUsedByVms() {
        passthroughNotChangedIfUsedByVmsTest(true, false, true);
        assertThat(validator.passthroughNotChangedIfUsedByVms(), failsWithVnicProfileInUse());
    }

    @Test
    public void passthroughChangedNotUsedByVms() {
        passthroughNotChangedIfUsedByVmsTest(false, true, false);
        assertThat(validator.passthroughNotChangedIfUsedByVms(), isValid());
    }

    @Test
    public void passthroughNotChangedUsedByVms() {
        passthroughNotChangedIfUsedByVmsTest(true, true, true);
        assertThat(validator.passthroughNotChangedIfUsedByVms(), isValid());
    }

    @Test
    public void passthroughNotChangedNotUsedByVms() {
        passthroughNotChangedIfUsedByVmsTest(false, false, true);
        assertThat(validator.passthroughNotChangedIfUsedByVms(), isValid());
    }

    private void passthroughNotChangedIfUsedByVmsTest(boolean passthoughOld,
            boolean pasthroughNew,
            boolean profileUsedByVms) {
        VnicProfile updatedVnicProfile = mock(VnicProfile.class);
        when(vnicProfile.isPassthrough()).thenReturn(passthoughOld);
        when(updatedVnicProfile.isPassthrough()).thenReturn(pasthroughNew);
        when(vnicProfileDao.get(any())).thenReturn(updatedVnicProfile);

        mockVmsUsingVnicProfile(profileUsedByVms ? Collections.singletonList(mock(VM.class))
                : Collections.emptyList());
    }

    @Test
    public void passthroughProfileContainsPortMirroring() {
        passthroughProfileContainsSupportedPropertiesTest(true, true, null, null);
        assertThat(validator.passthroughProfileContainsSupportedProperties(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_PASSTHROUGH_PROFILE_CONTAINS_NOT_SUPPORTED_PROPERTIES));
    }

    @Test
    public void passthroughProfileContainsQos() {
        passthroughProfileContainsSupportedPropertiesTest(true, false, DEFAULT_GUID, null);
        assertThat(validator.passthroughProfileContainsSupportedProperties(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_PASSTHROUGH_PROFILE_CONTAINS_NOT_SUPPORTED_PROPERTIES));
    }

    @Test
    public void passthroughProfileContainsFilterId() {
        passthroughProfileContainsSupportedPropertiesTest(true, false, DEFAULT_GUID, INVALID_NETWORK_FILTER_ID);
        assertThat(validator.passthroughProfileContainsSupportedProperties(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_PASSTHROUGH_PROFILE_CONTAINS_NOT_SUPPORTED_PROPERTIES));
    }


    @Test
    public void passthroughProfileValidProperties() {
        passthroughProfileContainsSupportedPropertiesTest(true, false, null, null);
        assertThat(validator.passthroughProfileContainsSupportedProperties(), isValid());
    }

    @Test
    public void nonPassthroughProfileContainsPortMirroringAndQos() {
        passthroughProfileContainsSupportedPropertiesTest(false, true, DEFAULT_GUID, VALID_NETWORK_FILTER_ID);
        assertThat(validator.passthroughProfileContainsSupportedProperties(), isValid());
    }

    @Test
    public void testValidNetworkFilterIdUseDefaultNoFilterId(){
        assertThat(validator.validUseDefaultNetworkFilterFlag(true), isValid());
    }

    @Test
    public void testValidNetworkFilterIdUseDefaultWithFilterId() {
        initVnicProfileNetworkFilterId(VALID_NETWORK_FILTER_ID, DEFAULT_VNIC_PROFILE_NAME);
        assertThat(validator.validUseDefaultNetworkFilterFlag(true),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_INVALID_VNIC_PROFILE_NETWORK_ID_CONFIGURATION,
                        ReplacementUtils.createSetVariableString(VnicProfileValidator.VAR_VNIC_PROFILE_NAME,
                                DEFAULT_VNIC_PROFILE_NAME),
                        ReplacementUtils.createSetVariableString(VnicProfileValidator.VAR_NETWORK_FILTER_ID,
                                VALID_NETWORK_FILTER_ID)));
    }

    @Test
    public void testValidNetworkFilterIdNoDefaultNoFilterId(){
        assertThat(validator.validUseDefaultNetworkFilterFlag(false), isValid());
    }

    @Test
    public void testValidNetworkFilterIdNoDefaultWithFilterId(){
        when(vnicProfile.getNetworkFilterId()).thenReturn(Guid.newGuid());
        assertThat(validator.validUseDefaultNetworkFilterFlag(false), isValid());
    }

    @Test
    public void validVnicProfileNullNetworkFilterId() {
        when(vnicProfile.getNetworkFilterId()).thenReturn(null);
        assertThat(validator.validNetworkFilterId(), isValid());
    }

    @Test
    public void validVnicProfileNetworkFilterId() {
        when(vnicProfile.getNetworkFilterId()).thenReturn(VALID_NETWORK_FILTER_ID);
        assertThat(validator.validNetworkFilterId(), isValid());
    }

    @Test
    public void invalidVnicProfileNetworkFilterId() {
        initVnicProfileNetworkFilterId(INVALID_NETWORK_FILTER_ID, DEFAULT_VNIC_PROFILE_NAME);
        assertThat(validator.validNetworkFilterId(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_INVALID_VNIC_PROFILE_NETWORK_FILTER_ID,
                        ReplacementUtils.createSetVariableString(VnicProfileValidator.VAR_VNIC_PROFILE_NAME,
                                DEFAULT_VNIC_PROFILE_NAME),
                        ReplacementUtils.createSetVariableString(VnicProfileValidator.VAR_NETWORK_FILTER_ID,
                                INVALID_NETWORK_FILTER_ID)));
    }

    private void initVnicProfileNetworkFilterId(Guid networkFilterId, String name) {
        when(vnicProfile.getNetworkFilterId()).thenReturn(networkFilterId);
        when(vnicProfile.getName()).thenReturn(name);
    }

    private void passthroughProfileContainsSupportedPropertiesTest(boolean passthrough,
            boolean portMirroring,
            Guid qosId,
            Guid networkFilterId) {
        when(vnicProfile.isPassthrough()).thenReturn(passthrough);
        when(vnicProfile.isPortMirroring()).thenReturn(portMirroring);
        when(vnicProfile.getNetworkQosId()).thenReturn(qosId);
        when(vnicProfile.getNetworkFilterId()).thenReturn(networkFilterId);
    }

    @Test
    public void emptyFailoverIdSuccess() {
        when(vnicProfile.getFailoverVnicProfileId()).thenReturn(null);
        assertThat(validator.validFailoverId(), isValid());
    }

    @Test
    public void failoverProfilePassthroughFail() {
        VnicProfile failoverProfile = new VnicProfile();
        failoverProfile.setPassthrough(true);

        when(vnicProfile.getFailoverVnicProfileId()).thenReturn(OTHER_GUID);
        when(vnicProfileDao.get(OTHER_GUID)).thenReturn(failoverProfile);

        assertThat(validator.validFailoverId(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_FAILOVER_VNIC_PROFILE_ID_IS_NOT_VALID));
    }

    @Test
    public void failoverProfileExternalNetworkFail() {
        Guid networkId = Guid.newGuid();
        VnicProfile failoverProfile = new VnicProfile();
        failoverProfile.setNetworkId(networkId);
        Network externalNetwork = new Network();
        externalNetwork.setProvidedBy(new ProviderNetwork());

        when(vnicProfile.getFailoverVnicProfileId()).thenReturn(OTHER_GUID);
        when(vnicProfileDao.get(OTHER_GUID)).thenReturn(failoverProfile);
        when(networkDao.get(networkId)).thenReturn(externalNetwork);

        assertThat(validator.validFailoverId(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_FAILOVER_VNIC_PROFILE_NOT_SUPPORTED_WITH_EXTERNAL_NETWORK));
    }

    @Test
    public void failoverProfileSuccess() {
        testProfileWithFailover(isValid(), true, true);
    }

    @Test
    public void profileWithFailoverNotPassthroughFail() {
        testProfileWithFailover(failsWith(EngineMessage.ACTION_TYPE_FAILED_FAILOVER_IS_SUPPORTED_ONLY_FOR_MIGRATABLE_PASSTROUGH),
                false,
                true);
    }

    @Test
    public void profileWithFailoverNotMigratableFail() {
        testProfileWithFailover(failsWith(EngineMessage.ACTION_TYPE_FAILED_FAILOVER_IS_SUPPORTED_ONLY_FOR_MIGRATABLE_PASSTROUGH),
                true,
                false);
    }

    @Test
    public void failoverIdPointingToSelfFail() {
        when(vnicProfile.getFailoverVnicProfileId()).thenReturn(DEFAULT_GUID);
        when(vnicProfile.getId()).thenReturn(DEFAULT_GUID);
        assertThat(validator.validFailoverId(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_FAILOVER_VNIC_PROFILE_ID_CANNOT_POINT_TO_SELF));
    }

    private void testProfileWithFailover(Matcher<ValidationResult> matcher, boolean passthrough, boolean migratable) {
        when(vnicProfile.isPassthrough()).thenReturn(passthrough);
        when(vnicProfile.isMigratable()).thenReturn(migratable);
        when(vnicProfile.getFailoverVnicProfileId()).thenReturn(OTHER_GUID);
        when(vnicProfileDao.get(OTHER_GUID)).thenReturn(new VnicProfile());
        when(networkDao.get(any())).thenReturn(new Network());

        assertThat(validator.validFailoverId(), matcher);
    }

    @Test
    public void failoverDidNotChange() {
        assertThat(validator.failoverNotChangedIfUsedByVms(), isValid());
    }

    @Test
    public void failoverChangeNotSupported() {
        VnicProfile updatedVnicProfile = mock(VnicProfile.class);
        when(vnicProfile.getFailoverVnicProfileId()).thenReturn(DEFAULT_GUID);
        when(updatedVnicProfile.getFailoverVnicProfileId()).thenReturn(OTHER_GUID);
        when(vnicProfileDao.get(any())).thenReturn(updatedVnicProfile);

        mockVmsUsingVnicProfile(Collections.singletonList(mock(VM.class)));
        assertThat(validator.failoverNotChangedIfUsedByVms(), failsWithVnicProfileInUse());
    }

}
