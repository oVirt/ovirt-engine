package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.CoreMatchers.is;
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
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.IscsiBondDao;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;

@RunWith(MockitoJUnitRunner.class)
public class NetworkValidatorTest {

    private final String NAMEABLE_NAME = "nameable";
    private final String DEFAULT_NETWORK_NAME = "mynetwork";
    private final String OTHER_NETWORK_NAME = "myothernetwork";
    private final Guid DEFAULT_GUID = Guid.newGuid();
    private final Guid OTHER_GUID = Guid.newGuid();
    private final int DEFAULT_VLAN_ID = 0;
    private final int OTHER_VLAN_ID = 1;

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule();

    @Mock
    private DbFacade dbFacade;

    @Mock
    private StoragePoolDAO dataCenterDao;

    @Mock
    private NetworkDao networkDao;

    @Mock
    private Network network;

    @Mock
    private StoragePool dataCenter;

    private List<Network> networks = new ArrayList<Network>();

    private NetworkValidator validator;

    @Before
    public void setup() {

        // spy on attempts to access the database
        validator = spy(new NetworkValidator(network));
        doReturn(dbFacade).when(validator).getDbFacade();

        // mock some commonly used DAOs
        when(dbFacade.getStoragePoolDao()).thenReturn(dataCenterDao);
        when(dbFacade.getNetworkDao()).thenReturn(networkDao);

        // mock their getters
        when(dataCenterDao.get(any(Guid.class))).thenReturn(dataCenter);
        when(networkDao.getAllForDataCenter(any(Guid.class))).thenReturn(networks);

        // mock version checking
        Version version = mock(Version.class);
        when(dataCenter.getcompatibility_version()).thenReturn(version);
    }

    @Test
    public void networkSet() throws Exception {
        assertThat(validator.networkIsSet(), isValid());
    }

    @Test
    public void networkNull() throws Exception {
        validator = new NetworkValidator(null);
        assertThat(validator.networkIsSet(), failsWith(VdcBllMessages.NETWORK_NOT_EXISTS));
    }

    @Test
    public void dataCenterDoesntExist() throws Exception {
        when(dataCenterDao.get(any(Guid.class))).thenReturn(null);
        assertThat(validator.dataCenterExists(), failsWith(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST));
    }

    @Test
    public void dataCenterExists() throws Exception {
        assertThat(validator.dataCenterExists(), isValid());
    }

    private void vmNetworkSetupTest(Matcher<ValidationResult> matcher, boolean vmNetwork, boolean featureSupported) {
        mockConfigRule.mockConfigValue(ConfigValues.NonVmNetworkSupported,
                dataCenter.getcompatibility_version(),
                featureSupported);
        when(network.isVmNetwork()).thenReturn(vmNetwork);

        assertThat(validator.vmNetworkSetCorrectly(), matcher);
    }

    @Test
    public void vmNetworkWhenSupported() throws Exception {
        vmNetworkSetupTest(isValid(), true, true);
    }

    @Test
    public void vmNetworkWhenNotSupported() throws Exception {
        vmNetworkSetupTest(isValid(), true, false);
    }

    @Test
    public void nonVmNetworkWhenSupported() throws Exception {
        vmNetworkSetupTest(isValid(), false, true);
    }

    @Test
    public void nonVmNetworkWhenNotSupported() throws Exception {
        vmNetworkSetupTest(failsWith(VdcBllMessages.NON_VM_NETWORK_NOT_SUPPORTED_FOR_POOL_LEVEL),
                false,
                false);
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
        stpTest(failsWith(VdcBllMessages.NON_VM_NETWORK_CANNOT_SUPPORT_STP), false, true);
    }

    @Test
    public void noStpWhenNonVmNetwork() throws Exception {
        stpTest(isValid(), false, false);
    }

    private void mtuValidTest(Matcher<ValidationResult> matcher, int mtu, boolean featureSupported) {
        mockConfigRule.mockConfigValue(ConfigValues.MTUOverrideSupported,
                dataCenter.getcompatibility_version(),
                featureSupported);
        when(network.getMtu()).thenReturn(mtu);

        assertThat(validator.mtuValid(), matcher);
    }

    @Test
    public void nonZeroMtuWhenSupported() throws Exception {
        mtuValidTest(isValid(), 1, true);
    }

    @Test
    public void nonZeroMtuWhenNotSupported() throws Exception {
        mtuValidTest(failsWith(VdcBllMessages.NETWORK_MTU_OVERRIDE_NOT_SUPPORTED), 1, false);
    }

    @Test
    public void zeroMtuWhenSupported() throws Exception {
        mtuValidTest(isValid(), 0, true);
    }

    @Test
    public void zeroMtuWhenNotSupported() throws Exception {
        mtuValidTest(isValid(), 0, false);
    }

    private void networkPrefixValidTest(Matcher<ValidationResult> matcher, String networkName) {
        when(network.getName()).thenReturn(networkName);

        assertThat(validator.networkPrefixValid(), matcher);
    }

    @Test
    public void networkPrefixBond() throws Exception {
        networkPrefixValidTest(failsWith(VdcBllMessages.NETWORK_CANNOT_CONTAIN_BOND_NAME), "bond0");
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

    private List<Network> getSingletonVlanNetworkList(int vlanId, Guid networkId) {
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
        vlanIdAvailableTest(failsWith(VdcBllMessages.NETWORK_VLAN_IN_USE),
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

    private List<Network> getSingletonNamedNetworkList(String networkName, Guid networkId) {
        Network network = mock(Network.class);
        when(network.getName()).thenReturn(networkName);
        when(network.getId()).thenReturn(networkId);
        return Collections.singletonList(network);
    }

    private List<IscsiBond> getIscsiBondList() {
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
        notIscsiBondNetworkTest(failsWith(VdcBllMessages.NETWORK_CANNOT_REMOVE_ISCSI_BOND_NETWORK), getIscsiBondList());
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
        networkNameAvailableTest(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_NAME_IN_USE),
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

    private Matcher<ValidationResult> failsWithNetworkInUse() {
        return failsWith(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_IN_USE);
    }

    private void networkNotUsedByVmsTest(Matcher<ValidationResult> matcher, List<VM> vms) {
        VmDAO vmDao = mock(VmDAO.class);
        when(vmDao.getAllForNetwork(any(Guid.class))).thenReturn(vms);
        when(dbFacade.getVmDao()).thenReturn(vmDao);
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
        VdsDAO hostDao = mock(VdsDAO.class);
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
        VmTemplateDAO templateDao = mock(VmTemplateDAO.class);
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
        assertThat(validator.notLabeled(), failsWith(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_ALREADY_LABELED));
    }

    @Test
    public void testNotExternalNetworkFailsForExternalNetwork() throws Exception {
        when(network.isExternal()).thenReturn(true);
        assertThat(validator.notExternalNetwork(), failsWith(VdcBllMessages.ACTION_TYPE_FAILED_NOT_SUPPORTED_FOR_EXTERNAL_NETWORK));
    }

    @Test
    public void testNotExternalNetworkSucceedsForNonExternalNetwork() throws Exception {
        when(network.isExternal()).thenReturn(false);
        assertThat(validator.notExternalNetwork(), is(ValidationResult.VALID));
    }

}
