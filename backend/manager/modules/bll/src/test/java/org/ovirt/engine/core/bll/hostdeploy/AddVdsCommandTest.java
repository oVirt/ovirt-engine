package org.ovirt.engine.core.bll.hostdeploy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.EngineSSHClient;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.action.hostdeploy.AddVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.ExternalComputeResource;
import org.ovirt.engine.core.common.businessentities.ExternalHostGroup;
import org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class AddVdsCommandTest {
    private static final String PEER_1 = "peer1";
    private static final Guid vdsId = Guid.newGuid();
    private AddVdsActionParameters parameters;

    @Mock
    private AddVdsCommand<AddVdsActionParameters> commandMock;
    @Mock
    private ClusterDao groupDaoMock;
    @Mock
    private VdsDao vdsDaoMock;
    @Mock
    private ClusterUtils clusterUtils;
    @Mock
    private GlusterUtil glusterUtil;
    @Mock
    private GlusterDBUtils glusterDBUtils;
    @Mock
    private EngineSSHClient sshClient;
    @Mock
    private Logger log;
    @Mock
    private HostValidator validator;

    @ClassRule
    public static MockConfigRule configRule =
            new MockConfigRule(MockConfigRule.mockConfig(ConfigValues.MaxVdsNameLength, 4),
                    MockConfigRule.mockConfig(ConfigValues.EncryptHostCommunication, false),
                    MockConfigRule.mockConfig(ConfigValues.InstallVds, true));

    private VDS makeTestVds(Guid vdsId) {
        VDS newVdsData = new VDS();
        newVdsData.setHostName("BUZZ");
        newVdsData.setSshPort(22);
        newVdsData.setSshUsername("root");
        newVdsData.setSshKeyFingerprint("1234");
        newVdsData.setVdsName("BAR");
        newVdsData.setClusterCompatibilityVersion(new Version("1.2.3"));
        newVdsData.setClusterId(Guid.newGuid());
        newVdsData.setId(vdsId);
        return newVdsData;
    }

    @Before
    public void createParameters() {
        parameters = new AddVdsActionParameters();
        parameters.setPassword("secret");
        VDS newVds = makeTestVds(vdsId);
        parameters.setvds(newVds);
    }

    private void setupCommonMock(boolean glusterEnabled) throws Exception {
        mockHostValidator();
        when(commandMock.validate()).thenCallRealMethod();
        when(commandMock.canConnect(any(VDS.class))).thenCallRealMethod();
        when(commandMock.getParameters()).thenReturn(parameters);

        when(commandMock.isGlusterSupportEnabled()).thenReturn(glusterEnabled);
        when(commandMock.getClusterDao()).thenReturn(groupDaoMock);
        when(commandMock.getClusterUtils()).thenReturn(clusterUtils);

        when(vdsDaoMock.get(vdsId)).thenReturn(null);
        when(commandMock.getVdsDao()).thenReturn(vdsDaoMock);
        when(commandMock.validateCluster()).thenReturn(true);
        when(commandMock.isPowerManagementLegal(any(Boolean.class), anyListOf(FenceAgent.class), any(String.class))).thenReturn(true);
        when(commandMock.getSSHClient()).thenReturn(sshClient);
        Version version = new Version("1.2.3");
        Cluster cluster = new Cluster();
        cluster.setCompatibilityVersion(version);
        when(commandMock.getCluster()).thenReturn(cluster);
        when(commandMock.isPowerManagementLegal(parameters.getVdsStaticData().isPmEnabled(),
                parameters.getFenceAgents(),
                new Version("1.2.3").toString())).thenReturn(true);
        doNothing().when(sshClient).connect();
        doNothing().when(sshClient).authenticate();
    }

    private void mockHostValidator() {
        when(validator.nameNotEmpty()).thenReturn(ValidationResult.VALID);
        doReturn(ValidationResult.VALID).when(validator).nameLengthIsLegal();
        doReturn(ValidationResult.VALID).when(validator).hostNameIsValid();
        doReturn(ValidationResult.VALID).when(validator).nameNotUsed();
        doReturn(ValidationResult.VALID).when(validator).hostNameNotUsed();
        doReturn(ValidationResult.VALID).when(validator).portIsValid();
        when(validator.sshUserNameNotEmpty()).thenReturn(ValidationResult.VALID);
        doReturn(ValidationResult.VALID).when(validator).validateSingleHostAttachedToLocalStorage();
        doReturn(ValidationResult.VALID).when(validator).securityKeysExists();
        doReturn(ValidationResult.VALID).when(validator).provisioningComputeResourceValid(any(Boolean.class),
                any(ExternalComputeResource.class));
        doReturn(ValidationResult.VALID).when(validator).provisioningHostGroupValid(any(Boolean.class),
                any(ExternalHostGroup.class));
        doReturn(ValidationResult.VALID).when(validator).protocolIsNotXmlrpc();
        doReturn(ValidationResult.VALID).when(validator).supportsDeployingHostedEngine(any(HostedEngineDeployConfiguration.class));
        when(validator.passwordNotEmpty(any(Boolean.class),
                any(AuthenticationMethod.class),
                any(String.class))).thenReturn(ValidationResult.VALID);
        when(commandMock.getHostValidator()).thenReturn(validator);
        when(commandMock.validate(any(ValidationResult.class))).thenCallRealMethod();
    }

    private void setupVirtMock() throws Exception {
        setupCommonMock(false);
    }

    private void setupGlusterMock(boolean clusterHasServers, VDS upServer, boolean hasPeers) throws Exception {
        setupCommonMock(true);

        when(commandMock.createReturnValue()).thenCallRealMethod();
        when(commandMock.getReturnValue()).thenCallRealMethod();
        doCallRealMethod().when(commandMock).addValidationMessage(any(EngineMessage.class));

        when(commandMock.getGlusterUtil()).thenReturn(glusterUtil);
        when(glusterUtil.getPeers(any(EngineSSHClient.class))).thenReturn(hasPeers ? Collections.singleton(PEER_1)
                : Collections.<String> emptySet());

        when(commandMock.getGlusterDBUtils()).thenReturn(glusterDBUtils);

        when(clusterUtils.hasServers(any(Guid.class))).thenReturn(clusterHasServers);
        when(vdsDaoMock.getAllForCluster(any(Guid.class))).thenReturn(mockVdsInDb(clusterHasServers ? VDSStatus.Maintenance
                : VDSStatus.Initializing));
        when(glusterUtil.getUpServer(any(Guid.class))).thenReturn(upServer);

        //commandMock.log = log;
    }

    private List<VDS> mockVdsInDb(VDSStatus status) {
        List<VDS> vdsList = new ArrayList<>();
        VDS vds = new VDS();
        vds.setStatus(status);
        vdsList.add(vds);
        return vdsList;
    }

    @Test
    public void validateVirtOnlySucceeds() throws Exception {
        setupVirtMock();
        assertTrue(commandMock.validate());
    }

    @Test
    public void validateSucceedsOnEmptyClusterEvenWhenGlusterServerHasPeers() throws Exception {
        setupGlusterMock(false, null, true);
        assertTrue(commandMock.validate());
    }

    @Test
    @Ignore("Cannot mock the log")
    public void validateSucceedsWhenHasPeersThrowsException() throws Exception {
        setupGlusterMock(true, new VDS(), true);
        when(glusterUtil.getPeers(any(EngineSSHClient.class))).thenThrow(new RuntimeException());
        assertTrue(commandMock.validate());
    }

    @Test
    public void validateFailsWhenGlusterServerHasPeers() throws Exception {
        setupGlusterMock(true, new VDS(), true);
        when(glusterDBUtils.serverExists(any(Guid.class), eq(PEER_1))).thenReturn(false);

        assertFalse(commandMock.validate());
        assertTrue(commandMock.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.SERVER_ALREADY_PART_OF_ANOTHER_CLUSTER.toString()));
    }

    @Test
    public void validateSucceedsWhenGlusterServerHasPeersThatExistInDB() throws Exception {
        setupGlusterMock(true, new VDS(), true);
        when(glusterDBUtils.serverExists(any(Guid.class), eq(PEER_1))).thenReturn(true);

        assertTrue(commandMock.validate());
    }

    @Test
    public void validateSucceedsWhenGlusterServerHasNoPeers() throws Exception {
        setupGlusterMock(true, new VDS(), false);
        assertTrue(commandMock.validate());
    }

    @Test
    public void validateSuccessForGlusterServerWhenUpServerExists() throws Exception {
        setupGlusterMock(true, new VDS(), false);
        assertTrue(commandMock.validate());
    }

    @Test
    public void validateFailsForGlusterServerWhenNoUpServer() throws Exception {
        setupGlusterMock(true, null, false);
        assertFalse(commandMock.validate());
        assertTrue(commandMock.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_NO_GLUSTER_HOST_TO_PEER_PROBE.toString()));
    }

    @Test
    public void provisioningValidated() throws Exception {
        setupVirtMock();
        assertTrue(commandMock.validate());
        verify(validator, times(1)).provisioningComputeResourceValid(any(Boolean.class),
                any(ExternalComputeResource.class));
        verify(validator, times(1)).provisioningHostGroupValid(any(Boolean.class), any(ExternalHostGroup.class));
    }

}
