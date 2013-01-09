package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class NetworkValidatorTest {

    private final String NAMEABLE_NAME = "nameable";
    private final String DEFAULT_NETWORK_NAME = "mynetwork";
    private final String OTHER_NETWORK_NAME = "myothernetwork";
    private final Guid DEFAULT_GUID = Guid.NewGuid();
    private final Guid OTHER_GUID = Guid.NewGuid();
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
    private storage_pool dataCenter;

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
        assertEquals(ValidationResult.VALID, validator.networkIsSet());
    }

    @Test
    public void networkNull() throws Exception {
        validator = new NetworkValidator(null);
        assertEquals(new ValidationResult(VdcBllMessages.NETWORK_NOT_EXISTS), validator.networkIsSet());
    }

    @Test
    public void dataCenterDoesntExist() throws Exception {
        when(dataCenterDao.get(any(Guid.class))).thenReturn(null);
        assertEquals(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST),
                validator.dataCenterExists());
    }

    @Test
    public void dataCenterExists() throws Exception {
        assertEquals(ValidationResult.VALID, validator.dataCenterExists());
    }

    private void vmNetworkSetupTest(ValidationResult expected, boolean vmNetwork, boolean featureSupported) {
        mockConfigRule.mockConfigValue(ConfigValues.NonVmNetworkSupported,
                dataCenter.getcompatibility_version(),
                featureSupported);
        when(network.isVmNetwork()).thenReturn(vmNetwork);

        assertEquals(expected, validator.vmNetworkSetCorrectly());
    }

    @Test
    public void vmNetworkWhenSupported() throws Exception {
        vmNetworkSetupTest(ValidationResult.VALID, true, true);
    }

    @Test
    public void vmNetworkWhenNotSupported() throws Exception {
        vmNetworkSetupTest(ValidationResult.VALID, true, false);
    }

    @Test
    public void nonVmNetworkWhenSupported() throws Exception {
        vmNetworkSetupTest(ValidationResult.VALID, false, true);
    }

    @Test
    public void nonVmNetworkWhenNotSupported() throws Exception {
        vmNetworkSetupTest(new ValidationResult(VdcBllMessages.NON_VM_NETWORK_NOT_SUPPORTED_FOR_POOL_LEVEL),
                false,
                false);
    }

    private void stpTest(ValidationResult expected, boolean vmNetwork, boolean stp) {
        when(network.isVmNetwork()).thenReturn(vmNetwork);
        when(network.getStp()).thenReturn(stp);

        assertEquals(expected, validator.stpForVmNetworkOnly());
    }

    @Test
    public void stpWhenVmNetwork() throws Exception {
        stpTest(ValidationResult.VALID, true, true);
    }

    @Test
    public void noStpWhenVmNetwork() throws Exception {
        stpTest(ValidationResult.VALID, true, false);
    }

    @Test
    public void stpWhenNonVmNetwork() throws Exception {
        stpTest(new ValidationResult(VdcBllMessages.NON_VM_NETWORK_CANNOT_SUPPORT_STP), false, true);
    }

    @Test
    public void noStpWhenNonVmNetwork() throws Exception {
        stpTest(ValidationResult.VALID, false, false);
    }

    private void mtuValidTest(ValidationResult expected, int mtu, boolean featureSupported) {
        mockConfigRule.mockConfigValue(ConfigValues.MTUOverrideSupported,
                dataCenter.getcompatibility_version(),
                featureSupported);
        when(network.getMtu()).thenReturn(mtu);

        assertEquals(expected, validator.mtuValid());
    }

    @Test
    public void nonZeroMtuWhenSupported() throws Exception {
        mtuValidTest(ValidationResult.VALID, 1, true);
    }

    @Test
    public void nonZeroMtuWhenNotSupported() throws Exception {
        mtuValidTest(new ValidationResult(VdcBllMessages.NETWORK_MTU_OVERRIDE_NOT_SUPPORTED), 1, false);
    }

    @Test
    public void zeroMtuWhenSupported() throws Exception {
        mtuValidTest(ValidationResult.VALID, 0, true);
    }

    @Test
    public void zeroMtuWhenNotSupported() throws Exception {
        mtuValidTest(ValidationResult.VALID, 0, false);
    }

    private void networkPrefixValidTest(ValidationResult expected, String networkName) {
        when(network.getName()).thenReturn(networkName);

        assertEquals(expected, validator.networkPrefixValid());
    }

    @Test
    public void networkPrefixBond() throws Exception {
        networkPrefixValidTest(new ValidationResult(VdcBllMessages.NETWORK_CANNOT_CONTAIN_BOND_NAME), "bond0");
    }

    @Test
    public void networkPrefixInnocent() throws Exception {
        networkPrefixValidTest(ValidationResult.VALID, DEFAULT_NETWORK_NAME);
    }

    private void vlanIdAvailableTest(ValidationResult expected, List<Network> networks) {
        this.networks.addAll(networks);
        when(network.getVlanId()).thenReturn(DEFAULT_VLAN_ID);
        when(network.getId()).thenReturn(DEFAULT_GUID);

        assertEquals(expected.getMessage(), validator.vlanIdNotUsed().getMessage());
    }

    private List<Network> getSingletonVlanNetworkList(int vlanId, Guid networkId) {
        Network network = new Network();
        network.setVlanId(vlanId);
        network.setId(networkId);
        return Collections.singletonList(network);
    }

    @Test
    public void vlanIdNoNetworks() throws Exception {
        vlanIdAvailableTest(ValidationResult.VALID, Collections.<Network> emptyList());
    }

    @Test
    public void vlanIdAvailable() throws Exception {
        vlanIdAvailableTest(ValidationResult.VALID, getSingletonVlanNetworkList(OTHER_VLAN_ID, OTHER_GUID));
    }

    @Test
    public void vlanIdTakenByDifferentNetwork() throws Exception {
        vlanIdAvailableTest(new ValidationResult(VdcBllMessages.NETWORK_VLAN_IN_USE),
                getSingletonVlanNetworkList(DEFAULT_VLAN_ID, OTHER_GUID));
    }

    @Test
    public void vlanIdTakenBySameNetwork() throws Exception {
        vlanIdAvailableTest(ValidationResult.VALID, getSingletonVlanNetworkList(DEFAULT_VLAN_ID, DEFAULT_GUID));
    }

    private void networkNameAvailableTest(ValidationResult expected, List<Network> networks) {
        this.networks.addAll(networks);
        when(network.getName()).thenReturn(DEFAULT_NETWORK_NAME);
        when(network.getId()).thenReturn(DEFAULT_GUID);

        assertEquals(expected, validator.networkNameNotUsed());
    }

    private List<Network> getSingletonNamedNetworkList(String networkName, Guid networkId) {
        Network network = mock(Network.class);
        when(network.getName()).thenReturn(networkName);
        when(network.getId()).thenReturn(networkId);
        return Collections.singletonList(network);
    }

    @Test
    public void networkNameNoNetworks() throws Exception {
        networkNameAvailableTest(ValidationResult.VALID, Collections.<Network> emptyList());
    }

    @Test
    public void networkNameAvailable() throws Exception {
        networkNameAvailableTest(ValidationResult.VALID, getSingletonNamedNetworkList(OTHER_NETWORK_NAME, OTHER_GUID));
    }

    @Test
    public void networkNameTakenByDifferentNetwork() throws Exception {
        networkNameAvailableTest(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_NAME_IN_USE),
                getSingletonNamedNetworkList(DEFAULT_NETWORK_NAME, OTHER_GUID));
    }

    @Test
    public void networkNameTakenCaseSensitivelyByDifferentNetwork() throws Exception {
        networkNameAvailableTest(ValidationResult.VALID,
                getSingletonNamedNetworkList(DEFAULT_NETWORK_NAME.toUpperCase(), OTHER_GUID));
    }

    @Test
    public void networkNameTakenBySameNetwork() throws Exception {
        networkNameAvailableTest(ValidationResult.VALID,
                getSingletonNamedNetworkList(DEFAULT_NETWORK_NAME, DEFAULT_GUID));
    }

    private void networkNotUsedByVmsTest(ValidationResult expected, List<VM> vms) {
        VmDAO vmDao = mock(VmDAO.class);
        when(vmDao.getAllForNetwork(any(Guid.class))).thenReturn(vms);
        when(dbFacade.getVmDao()).thenReturn(vmDao);
        assertEquals(expected.getMessage(), validator.networkNotUsedByVms().getMessage());
    }

    @Test
    public void networkNotInUseByVms() throws Exception {
        networkNotUsedByVmsTest(ValidationResult.VALID, Collections.<VM> emptyList());
    }

    @Test
    public void networkInUseByVms() throws Exception {
        VM vm = mock(VM.class);
        when(vm.getName()).thenReturn(NAMEABLE_NAME);

        networkNotUsedByVmsTest(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_IN_USE),
                Collections.singletonList(vm));
    }

    private void networkNotUsedByHostsTest(ValidationResult expected, List<VDS> hosts) {
        VdsDAO hostDao = mock(VdsDAO.class);
        when(hostDao.getAllForNetwork(any(Guid.class))).thenReturn(hosts);
        when(dbFacade.getVdsDao()).thenReturn(hostDao);
        assertEquals(expected.getMessage(), validator.networkNotUsedByHosts().getMessage());
    }

    @Test
    public void networkNotInUseByHosts() throws Exception {
        networkNotUsedByHostsTest(ValidationResult.VALID, Collections.<VDS> emptyList());
    }

    @Test
    public void networkInUseByHosts() throws Exception {
        VDS host = mock(VDS.class);
        when(host.getName()).thenReturn(NAMEABLE_NAME);

        networkNotUsedByHostsTest(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_IN_USE),
                Collections.singletonList(host));
    }

    private void networkNotUsedByTemplatesTest(ValidationResult expected, List<VmTemplate> templates) {
        VmTemplateDAO templateDao = mock(VmTemplateDAO.class);
        when(templateDao.getAllForNetwork(any(Guid.class))).thenReturn(templates);
        when(dbFacade.getVmTemplateDao()).thenReturn(templateDao);
        assertEquals(expected.getMessage(), validator.networkNotUsedByTemplates().getMessage());
    }

    @Test
    public void networkNotInUseByTemplates() throws Exception {
        networkNotUsedByTemplatesTest(ValidationResult.VALID, Collections.<VmTemplate> emptyList());
    }

    @Test
    public void networkInUseByTemplates() throws Exception {
        VmTemplate template = mock(VmTemplate.class);
        when(template.getName()).thenReturn(NAMEABLE_NAME);

        networkNotUsedByTemplatesTest(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_IN_USE),
                Collections.singletonList(template));
    }
}
