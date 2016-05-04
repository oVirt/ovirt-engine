package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.NetworkFilterDao;
import org.ovirt.engine.core.dao.network.NetworkQoSDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.utils.ReplacementUtils;

@RunWith(MockitoJUnitRunner.class)
public class VnicProfileValidatorTest {

    private static final String NAMEABLE_NAME = "nameable";
    private static final String DEFAULT_VNIC_PROFILE_NAME = "myvnicprofile";
    private static final String OTHER_VNIC_PROFILE_NAME = "myothervnicprofile";
    private static final Guid DEFAULT_GUID = Guid.newGuid();
    private static final Guid OTHER_GUID = Guid.newGuid();
    private static final Guid VALID_NETWORK_FILTER_ID = Guid.newGuid();
    private static final Guid INVALID_NETWORK_FILTER_ID = Guid.newGuid();

    @Rule
    public MockConfigRule mcr = new MockConfigRule();

    @Mock
    private DbFacade dbFacade;

    @Mock
    private VnicProfileDao vnicProfileDao;

    @Mock
    private NetworkDao networkDao;

    @Mock
    private NetworkQoSDao networkQosDao;

    @Mock
    private VmDao vmDao;

    @Mock
    private StoragePoolDao dcDao;

    @Mock
    private NetworkFilterDao networkFilterDao;

    @Mock
    private VnicProfile vnicProfile;

    @Mock
    private Network network;

    @Mock
    private NetworkQoS networkQos;

    private List<VnicProfile> vnicProfiles = new ArrayList<>();

    private VnicProfileValidator validator;

    @Before
    public void setup() {

        // spy on attempts to access the database
        validator = spy(new VnicProfileValidator(vnicProfile, vmDao, dcDao, networkFilterDao));
        doReturn(dbFacade).when(validator).getDbFacade();

        // mock some commonly used Daos
        when(dbFacade.getVnicProfileDao()).thenReturn(vnicProfileDao);
        when(dbFacade.getNetworkDao()).thenReturn(networkDao);
        when(dbFacade.getNetworkQosDao()).thenReturn(networkQosDao);
        when(dbFacade.getVmDao()).thenReturn(vmDao);
        initNetworkFilterDao();

        // mock their getters
        when(vnicProfileDao.get(any(Guid.class))).thenReturn(vnicProfile);
        when(vnicProfileDao.getAllForNetwork(any(Guid.class))).thenReturn(vnicProfiles);
    }

    private void initNetworkFilterDao() {
        when(networkFilterDao.getNetworkFilterById(INVALID_NETWORK_FILTER_ID)).thenReturn(null);
        when(networkFilterDao.getNetworkFilterById(VALID_NETWORK_FILTER_ID))
                .thenReturn(new NetworkFilter(VALID_NETWORK_FILTER_ID));
    }

    @Test
    public void vnicProfileSet() throws Exception {
        assertThat(validator.vnicProfileIsSet(), isValid());
    }

    @Test
    public void vnicProfileNull() throws Exception {
        validator = new VnicProfileValidator(null, vmDao, dcDao, networkFilterDao);
        assertThat(validator.vnicProfileIsSet(), failsWith(EngineMessage.ACTION_TYPE_FAILED_VNIC_PROFILE_NOT_EXISTS));
    }

    @Test
    public void vnicProfileExists() throws Exception {
        assertThat(validator.vnicProfileExists(), isValid());
    }

    @Test
    public void vnicProfileDoesNotExist() throws Exception {
        when(vnicProfileDao.get(any(Guid.class))).thenReturn(null);
        assertThat(validator.vnicProfileExists(), failsWith(EngineMessage.ACTION_TYPE_FAILED_VNIC_PROFILE_NOT_EXISTS));
    }

    @Test
    public void networkExists() throws Exception {
        when(networkDao.get(any(Guid.class))).thenReturn(network);
        assertThat(validator.networkExists(), isValid());
    }

    @Test
    public void networkDoesntExist() throws Exception {
        when(networkDao.get(any(Guid.class))).thenReturn(null);
        assertThat(validator.networkExists(), failsWith(EngineMessage.NETWORK_HAVING_ID_NOT_EXISTS));
    }

    @Test
    public void networkQosExists() throws Exception {
        when(vnicProfile.getNetworkQosId()).thenReturn(DEFAULT_GUID);
        when(networkQosDao.get(DEFAULT_GUID)).thenReturn(networkQos);
        assertThat(validator.networkQosExistsOrNull(), isValid());
    }

    @Test
    public void networkQosNull() throws Exception {
        when(vnicProfile.getNetworkQosId()).thenReturn(null);
        assertThat(validator.networkQosExistsOrNull(), isValid());
    }

    @Test
    public void networkQosDoesntExist() throws Exception {
        when(vnicProfile.getNetworkQosId()).thenReturn(DEFAULT_GUID);
        when(networkQosDao.get(any(Guid.class))).thenReturn(null);
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
    public void vnicProfileNameNoVnicProfiles() throws Exception {
        vnicProfileAvailableTest(isValid(), Collections.<VnicProfile> emptyList());
    }

    @Test
    public void vnicProfileNameAvailable() throws Exception {
        vnicProfileAvailableTest(isValid(), getSingletonNamedVnicProfileList(OTHER_VNIC_PROFILE_NAME, OTHER_GUID));
    }

    @Test
    public void vnicProfileNameTakenByDifferentVnicProfile() throws Exception {
        vnicProfileAvailableTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_VNIC_PROFILE_NAME_IN_USE),
                getSingletonNamedVnicProfileList(DEFAULT_VNIC_PROFILE_NAME, OTHER_GUID));
    }

    @Test
    public void vnicProfileNameTakenCaseSensitivelyByDifferentVnicProfile() throws Exception {
        vnicProfileAvailableTest(isValid(),
                getSingletonNamedVnicProfileList(DEFAULT_VNIC_PROFILE_NAME.toUpperCase(), OTHER_GUID));
    }

    @Test
    public void vnicProfileNameTakenBySameVnicProfile() throws Exception {
        vnicProfileAvailableTest(isValid(),
                getSingletonNamedVnicProfileList(DEFAULT_VNIC_PROFILE_NAME, DEFAULT_GUID));
    }

    private static Matcher<ValidationResult> failsWithVnicProfileInUse() {
        return failsWith(EngineMessage.ACTION_TYPE_FAILED_VNIC_PROFILE_IN_ONE_USE);
    }

    @Test
    public void networkChanged() throws Exception {
        mockVnicProfileNetworkChange(DEFAULT_GUID, DEFAULT_GUID);
        assertThat(validator.networkNotChanged(), isValid());
    }

    @Test
    public void changingNetworkNotAllowed() throws Exception {
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
        mockVmsUsingVnicProfile(Collections.<VM> emptyList());
        assertThat(validator.portMirroringNotChangedIfUsedByVms(), isValid());
    }

    @Test
    public void portMirroringChangeNotSupported() {
        mockVnicProfilePortMirroringChange(false);
        mockVmsUsingVnicProfile(Collections.<VM> singletonList(mock(VM.class)));
        assertThat(validator.portMirroringNotChangedIfUsedByVms(), failsWithVnicProfileInUse());
    }

    private void mockVnicProfileNetworkChange(Guid vnicProfileId, Guid oldVnicProfileId) {
        VnicProfile vnicProfile = mock(VnicProfile.class);
        when(this.vnicProfile.getNetworkId()).thenReturn(vnicProfileId);
        when(vnicProfile.getNetworkId()).thenReturn(oldVnicProfileId);
        when(vnicProfileDao.get(any(Guid.class))).thenReturn(vnicProfile);
    }

    private void vnicProfileNotUsedByVmsTest(Matcher<ValidationResult> matcher, List<VM> vms) {
        mockVmsUsingVnicProfile(vms);
        assertThat(validator.vnicProfileNotUsedByVms(), matcher);
    }

    private void mockVmsUsingVnicProfile(List<VM> vms) {
        when(vmDao.getAllForVnicProfile(any(Guid.class))).thenReturn(vms);
    }

    private void mockVnicProfilePortMirroringChange(boolean portMirroring) {
        VnicProfile vnicProfile = mock(VnicProfile.class);
        when(this.vnicProfile.isPortMirroring()).thenReturn(portMirroring);
        when(vnicProfile.isPortMirroring()).thenReturn(!portMirroring);
        when(vnicProfileDao.get(any(Guid.class))).thenReturn(vnicProfile);
    }

    @Test
    public void vnicProfileNotInUseByVms() throws Exception {
        vnicProfileNotUsedByVmsTest(isValid(), Collections.<VM> emptyList());
    }

    @Test
    public void vnicProfileInUseByVms() throws Exception {
        VM vm = mock(VM.class);
        when(vm.getName()).thenReturn(NAMEABLE_NAME);
        vnicProfileNotUsedByVmsTest(failsWithVnicProfileInUse(), Collections.singletonList(vm));
    }

    private void vnicProfileNotUsedByTemplatesTest(Matcher<ValidationResult> matcher, List<VmTemplate> templates) {
        VmTemplateDao templateDao = mock(VmTemplateDao.class);
        when(templateDao.getAllForVnicProfile(any(Guid.class))).thenReturn(templates);
        when(dbFacade.getVmTemplateDao()).thenReturn(templateDao);
        assertThat(validator.vnicProfileNotUsedByTemplates(), matcher);
    }

    @Test
    public void vnicProfileNotInUseByTemplates() throws Exception {
        vnicProfileNotUsedByTemplatesTest(isValid(), Collections.<VmTemplate> emptyList());
    }

    @Test
    public void vnicProfileInUseByTemplates() throws Exception {
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
        when(networkDao.get(any(Guid.class))).thenReturn(network);
        assertThat(validator.vnicProfileForVmNetworkOnly(), matcher);
    }

    @Test
    public void externalNetworkPortMirroring() throws Exception {
        externalNetworkPortMirroringTest(true,
                true,
                failsWith(EngineMessage.ACTION_TYPE_FAILED_EXTERNAL_NETWORK_CANNOT_BE_PORT_MIRRORED));
    }

    @Test
    public void externalNetworkNotPortMirroring() throws Exception {
        externalNetworkPortMirroringTest(true, false, isValid());
    }

    @Test
    public void internalNetworkPortMirroring() throws Exception {
        externalNetworkPortMirroringTest(false, true, isValid());
    }

    @Test
    public void internalNetworkNotPortMirroring() throws Exception {
        externalNetworkPortMirroringTest(false, false, isValid());
    }

    private void externalNetworkPortMirroringTest(boolean externalNetwork,
            boolean portMirroring,
            Matcher<ValidationResult> matcher) {
        when(networkDao.get(any(Guid.class))).thenReturn(network);
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
        when(vnicProfileDao.get(any(Guid.class))).thenReturn(updatedVnicProfile);

        mockVmsUsingVnicProfile(profileUsedByVms ? Collections.<VM> singletonList(mock(VM.class))
                : Collections.<VM> emptyList());
    }

    @Test
    public void passthroughProfileContainsPortMirroring() {
        passthroughProfileContainsSupportedPropertiesTest(true, true, null);
        assertThat(validator.passthroughProfileContainsSupportedProperties(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_PASSTHROUGH_PROFILE_CONTAINS_NOT_SUPPORTED_PROPERTIES));
    }

    @Test
    public void passthroughProfileContainsQos() {
        passthroughProfileContainsSupportedPropertiesTest(true, false, DEFAULT_GUID);
        assertThat(validator.passthroughProfileContainsSupportedProperties(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_PASSTHROUGH_PROFILE_CONTAINS_NOT_SUPPORTED_PROPERTIES));
    }

    @Test
    public void passthroughProfileValidProprerties() {
        passthroughProfileContainsSupportedPropertiesTest(true, false, null);
        assertThat(validator.passthroughProfileContainsSupportedProperties(), isValid());
    }

    @Test
    public void nonPassthroughProfileContainsPortMirroringAndQos() {
        passthroughProfileContainsSupportedPropertiesTest(false, true, DEFAULT_GUID);
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
            Guid qosId) {
        when(vnicProfile.isPassthrough()).thenReturn(passthrough);
        when(vnicProfile.isPortMirroring()).thenReturn(portMirroring);
        when(vnicProfile.getNetworkQosId()).thenReturn(qosId);
    }
}
