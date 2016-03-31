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
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.IscsiBondDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.RandomUtils;

@RunWith(MockitoJUnitRunner.class)
public class NetworkValidatorTest {

    private static final String NAMEABLE_NAME = "nameable";
    private static final String DEFAULT_NETWORK_NAME = "mynetwork";
    private static final String OTHER_NETWORK_NAME = "myothernetwork";
    private static final Guid DEFAULT_GUID = Guid.newGuid();
    private static final Guid OTHER_GUID = Guid.newGuid();
    private static final int DEFAULT_VLAN_ID = 0;
    private static final int OTHER_VLAN_ID = 1;

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule();

    @Mock
    private DbFacade dbFacade;

    @Mock
    private StoragePoolDao dataCenterDao;

    @Mock
    private NetworkDao networkDao;

    @Mock
    private Network network;

    @Mock
    private StoragePool dataCenter;

    @Mock
    private ManagementNetworkUtil managementNetworkUtil;

    @Mock
    private VmDao vmDao;

    private List<Network> networks = new ArrayList<>();
    private NetworkValidator validator;

    @Before
    public void setup() {

        // spy on attempts to access the database
        validator = spy(new NetworkValidator(vmDao, network));
        doReturn(dbFacade).when(validator).getDbFacade();
        doReturn(managementNetworkUtil).when(validator).getManagementNetworkUtil();

        // mock some commonly used Daos
        when(dbFacade.getStoragePoolDao()).thenReturn(dataCenterDao);
        when(dbFacade.getNetworkDao()).thenReturn(networkDao);

        // mock their getters
        when(dataCenterDao.get(any(Guid.class))).thenReturn(dataCenter);
        when(networkDao.getAllForDataCenter(any(Guid.class))).thenReturn(networks);

        // mock version checking
        Version version = mock(Version.class);
        when(dataCenter.getCompatibilityVersion()).thenReturn(version);
    }

    @Test
    public void networkSet() throws Exception {
        assertThat(validator.networkIsSet(Guid.newGuid()), isValid());
    }

    @Test
    public void networkNull() throws Exception {
        validator = new NetworkValidator(vmDao, null);
        assertThat(validator.networkIsSet(Guid.newGuid()), failsWith(EngineMessage.NETWORK_HAVING_ID_NOT_EXISTS));
    }

    @Test
    public void dataCenterDoesntExist() throws Exception {
        when(dataCenterDao.get(any(Guid.class))).thenReturn(null);
        assertThat(validator.dataCenterExists(), failsWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST));
    }

    @Test
    public void dataCenterExists() throws Exception {
        assertThat(validator.dataCenterExists(), isValid());
    }

    private void stpTest(Matcher<ValidationResult> matcher, boolean vmNetwork, boolean stp) {
        when(network.isVmNetwork()).thenReturn(vmNetwork);
        when(network.getStp()).thenReturn(stp);

        assertThat(validator.stpForVmNetworkOnly(), matcher);
    }

    @Test
    public void stpWhenVmNetwork() throws Exception {
        stpTest(isValid(), true, true);
    }

    @Test
    public void noStpWhenVmNetwork() throws Exception {
        stpTest(isValid(), true, false);
    }

    @Test
    public void stpWhenNonVmNetwork() throws Exception {
        stpTest(failsWith(EngineMessage.NON_VM_NETWORK_CANNOT_SUPPORT_STP), false, true);
    }

    @Test
    public void noStpWhenNonVmNetwork() throws Exception {
        stpTest(isValid(), false, false);
    }

    @Test
    public void mtuValid() {
        assertThat(validator.mtuValid(), isValid());
    }

    private void networkPrefixValidTest(Matcher<ValidationResult> matcher, String networkName) {
        when(network.getName()).thenReturn(networkName);

        assertThat(validator.networkPrefixValid(), matcher);
    }

    @Test
    public void networkPrefixBond() throws Exception {
        networkPrefixValidTest(failsWith(EngineMessage.NETWORK_CANNOT_CONTAIN_BOND_NAME), "bond0");
    }

    @Test
    public void networkPrefixInnocent() throws Exception {
        networkPrefixValidTest(isValid(), DEFAULT_NETWORK_NAME);
    }

    private void vlanIdAvailableTest(Matcher<ValidationResult> matcher, List<Network> networks) {
        this.networks.addAll(networks);
        when(network.getVlanId()).thenReturn(DEFAULT_VLAN_ID);
        when(network.getId()).thenReturn(DEFAULT_GUID);

        assertThat(validator.vlanIdNotUsed(), matcher);
    }

    private static List<Network> getSingletonVlanNetworkList(int vlanId, Guid networkId) {
        Network network = new Network();
        network.setVlanId(vlanId);
        network.setId(networkId);
        return Collections.singletonList(network);
    }

    @Test
    public void vlanIdNoNetworks() throws Exception {
        vlanIdAvailableTest(isValid(), Collections.<Network> emptyList());
    }

    @Test
    public void vlanIdAvailable() throws Exception {
        vlanIdAvailableTest(isValid(), getSingletonVlanNetworkList(OTHER_VLAN_ID, OTHER_GUID));
    }

    @Test
    public void vlanIdTakenByDifferentNetwork() throws Exception {
        vlanIdAvailableTest(failsWith(EngineMessage.NETWORK_VLAN_IN_USE),
                getSingletonVlanNetworkList(DEFAULT_VLAN_ID, OTHER_GUID));
    }

    @Test
    public void vlanIdTakenBySameNetwork() throws Exception {
        vlanIdAvailableTest(isValid(), getSingletonVlanNetworkList(DEFAULT_VLAN_ID, DEFAULT_GUID));
    }

    private void networkNameAvailableTest(Matcher<ValidationResult> matcher, List<Network> networks) {
        this.networks.addAll(networks);
        when(network.getName()).thenReturn(DEFAULT_NETWORK_NAME);
        when(network.getId()).thenReturn(DEFAULT_GUID);

        assertThat(validator.networkNameNotUsed(), matcher);
    }

    private void notIscsiBondNetworkTest(Matcher<ValidationResult> matcher, List<IscsiBond> iscsiBonds) {
        IscsiBondDao iscsiBondDao = mock(IscsiBondDao.class);
        when(iscsiBondDao.getIscsiBondsByNetworkId(any(Guid.class))).thenReturn(iscsiBonds);
        when(dbFacade.getIscsiBondDao()).thenReturn(iscsiBondDao);
        assertThat(validator.notIscsiBondNetwork(), matcher);
    }

    private static List<Network> getSingletonNamedNetworkList(String networkName, Guid networkId) {
        Network network = mock(Network.class);
        when(network.getName()).thenReturn(networkName);
        when(network.getId()).thenReturn(networkId);
        return Collections.singletonList(network);
    }

    private static List<IscsiBond> getIscsiBondList() {
        List<IscsiBond> iscsiBondList = new ArrayList<>();
        IscsiBond iscsiBond = new IscsiBond();
        iscsiBond.setId(Guid.newGuid());
        iscsiBond.setName("IscsiBond name");
        iscsiBondList.add(iscsiBond);
        return iscsiBondList;
    }

    @Test
    public void noIscsiBondsForNetowrkTest() throws Exception {
        notIscsiBondNetworkTest(isValid(), Collections.<IscsiBond> emptyList());
    }

    @Test
    public void existingIscsiBondsForNetowrkTest() throws Exception {
        notIscsiBondNetworkTest(failsWith(EngineMessage.NETWORK_CANNOT_REMOVE_ISCSI_BOND_NETWORK), getIscsiBondList());
    }

    @Test
    public void networkNameNoNetworks() throws Exception {
        networkNameAvailableTest(isValid(), Collections.<Network> emptyList());
    }

    @Test
    public void networkNameAvailable() throws Exception {
        networkNameAvailableTest(isValid(), getSingletonNamedNetworkList(OTHER_NETWORK_NAME, OTHER_GUID));
    }

    @Test
    public void networkNameTakenByDifferentNetwork() throws Exception {
        networkNameAvailableTest(failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_NAME_IN_USE),
                getSingletonNamedNetworkList(DEFAULT_NETWORK_NAME, OTHER_GUID));
    }

    @Test
    public void networkNameTakenCaseSensitivelyByDifferentNetwork() throws Exception {
        networkNameAvailableTest(isValid(),
                getSingletonNamedNetworkList(DEFAULT_NETWORK_NAME.toUpperCase(), OTHER_GUID));
    }

    @Test
    public void networkNameTakenBySameNetwork() throws Exception {
        networkNameAvailableTest(isValid(),
                getSingletonNamedNetworkList(DEFAULT_NETWORK_NAME, DEFAULT_GUID));
    }

    private static Matcher<ValidationResult> failsWithNetworkInUse() {
        return failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_IN_ONE_USE);
    }

    private void networkNotUsedByVmsTest(Matcher<ValidationResult> matcher, List<VM> vms) {
        when(vmDao.getAllForNetwork(any(Guid.class))).thenReturn(vms);
        assertThat(validator.networkNotUsedByVms(), matcher);
    }

    @Test
    public void networkNotInUseByVms() throws Exception {
        networkNotUsedByVmsTest(isValid(), Collections.<VM> emptyList());
    }

    @Test
    public void networkInUseByVms() throws Exception {
        VM vm = mock(VM.class);
        when(vm.getName()).thenReturn(NAMEABLE_NAME);

        networkNotUsedByVmsTest(failsWithNetworkInUse(), Collections.singletonList(vm));
    }

    private void networkNotUsedByHostsTest(Matcher<ValidationResult> matcher, List<VDS> hosts) {
        VdsDao hostDao = mock(VdsDao.class);
        when(hostDao.getAllForNetwork(any(Guid.class))).thenReturn(hosts);
        when(dbFacade.getVdsDao()).thenReturn(hostDao);
        assertThat(validator.networkNotUsedByHosts(), matcher);
    }

    @Test
    public void networkNotInUseByHosts() throws Exception {
        networkNotUsedByHostsTest(isValid(), Collections.<VDS> emptyList());
    }

    @Test
    public void networkInUseByHosts() throws Exception {
        VDS host = mock(VDS.class);
        when(host.getName()).thenReturn(NAMEABLE_NAME);

        networkNotUsedByHostsTest(failsWithNetworkInUse(), Collections.singletonList(host));
    }

    private void networkNotUsedByTemplatesTest(Matcher<ValidationResult> matcher, List<VmTemplate> templates) {
        VmTemplateDao templateDao = mock(VmTemplateDao.class);
        when(templateDao.getAllForNetwork(any(Guid.class))).thenReturn(templates);
        when(dbFacade.getVmTemplateDao()).thenReturn(templateDao);
        assertThat(validator.networkNotUsedByTemplates(), matcher);
    }

    @Test
    public void networkNotInUseByTemplates() throws Exception {
        networkNotUsedByTemplatesTest(isValid(), Collections.<VmTemplate> emptyList());
    }

    @Test
    public void networkInUseByTemplates() throws Exception {
        VmTemplate template = mock(VmTemplate.class);
        when(template.getName()).thenReturn(NAMEABLE_NAME);

        networkNotUsedByTemplatesTest(failsWithNetworkInUse(), Collections.singletonList(template));
    }

    @Test
    public void networkNotLabeled() throws Exception {
        assertThat(validator.notLabeled(), isValid());
    }

    @Test
    public void networkLabeled() throws Exception {
        when(network.getLabel()).thenReturn(RandomUtils.instance().nextPropertyString(10));
        assertThat(validator.notLabeled(), failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_ALREADY_LABELED));
    }

    @Test
    public void testNotExternalNetworkFailsForExternalNetwork() throws Exception {
        when(network.isExternal()).thenReturn(true);
        assertThat(validator.notExternalNetwork(), failsWith(EngineMessage.ACTION_TYPE_FAILED_NOT_SUPPORTED_FOR_EXTERNAL_NETWORK));
    }

    @Test
    public void testNotExternalNetworkSucceedsForNonExternalNetwork() throws Exception {
        when(network.isExternal()).thenReturn(false);
        assertThat(validator.notExternalNetwork(), isValid());
    }

    @Test
    public void testNotManagementNetworkPositive() {
        when(network.getId()).thenReturn(DEFAULT_GUID);
        when(managementNetworkUtil.isManagementNetwork(DEFAULT_GUID)).thenReturn(true);
        assertThat(validator.notManagementNetwork(), failsWith(EngineMessage.NETWORK_CANNOT_REMOVE_MANAGEMENT_NETWORK));
    }

    @Test
    public void testNotManagementNetworkNegative() {
        when(network.getId()).thenReturn(DEFAULT_GUID);
        when(managementNetworkUtil.isManagementNetwork(DEFAULT_GUID)).thenReturn(false);
        assertThat(validator.notManagementNetwork(), isValid());
    }

}
