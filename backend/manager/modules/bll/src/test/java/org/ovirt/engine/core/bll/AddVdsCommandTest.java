package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.EngineSSHClient;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.action.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class AddVdsCommandTest {
    private static final String PEER_1 = "peer1";
    private static final Guid vdsId = Guid.newGuid();
    private AddVdsActionParameters parameters;

    @Mock
    private AddVdsCommand<AddVdsActionParameters> commandMock;
    @Mock
    private VdsGroupDAO groupDAOMock;
    @Mock
    private VdsDAO vdsDaoMock;
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
        newVdsData.setVdsGroupCompatibilityVersion(new Version("1.2.3"));
        newVdsData.setVdsGroupId(Guid.newGuid());
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
        when(commandMock.canDoAction()).thenCallRealMethod();
        when(commandMock.canConnect(any(VDS.class))).thenCallRealMethod();
        when(commandMock.getParameters()).thenReturn(parameters);

        when(commandMock.isGlusterSupportEnabled()).thenReturn(glusterEnabled);
        when(commandMock.getVdsGroupDAO()).thenReturn(groupDAOMock);
        when(commandMock.getClusterUtils()).thenReturn(clusterUtils);

        when(vdsDaoMock.get(vdsId)).thenReturn(null);
        when(commandMock.getVdsDAO()).thenReturn(vdsDaoMock);
        when(commandMock.validateVdsGroup()).thenReturn(true);
        when(commandMock.isPowerManagementLegal(any(Boolean.class), anyListOf(FenceAgent.class), any(String.class))).thenReturn(true);
        when(commandMock.getSSHClient()).thenReturn(sshClient);
        Version version = new Version("1.2.3");
        VDSGroup vdsGroup = new VDSGroup();
        vdsGroup.setcompatibility_version(version);
        when(commandMock.getVdsGroup()).thenReturn(vdsGroup);
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
        when(validator.passwordNotEmpty(any(Boolean.class),
                any(AuthenticationMethod.class),
                any(String.class))).thenReturn(ValidationResult.VALID);
        when(commandMock.getHostValidator()).thenReturn(validator);
        when(commandMock.validate(any(ValidationResult.class))).thenCallRealMethod();
    }

    private void setupVirtMock() throws Exception {
        setupCommonMock(false);
    }

    @SuppressWarnings("unchecked")
    private void setupGlusterMock(boolean clusterHasServers, VDS upServer, boolean hasPeers) throws Exception {
        setupCommonMock(true);

        when(commandMock.createReturnValue()).thenCallRealMethod();
        when(commandMock.getReturnValue()).thenCallRealMethod();
        doCallRealMethod().when(commandMock).addCanDoActionMessage(any(VdcBllMessages.class));

        when(commandMock.getGlusterUtil()).thenReturn(glusterUtil);
        when(glusterUtil.getPeers(any(EngineSSHClient.class))).thenReturn(hasPeers ? Collections.singleton(PEER_1)
                : Collections.EMPTY_SET);

        when(commandMock.getGlusterDBUtils()).thenReturn(glusterDBUtils);

        when(clusterUtils.hasServers(any(Guid.class))).thenReturn(clusterHasServers);
        when(clusterUtils.getUpServer(any(Guid.class))).thenReturn(upServer);

        commandMock.log = this.log;
    }

    @Test
    public void canDoActionVirtOnlySucceeds() throws Exception {
        setupVirtMock();
        assertTrue(commandMock.canDoAction());
    }

    @Test
    public void canDoActionSucceedsOnEmptyClusterEvenWhenGlusterServerHasPeers() throws Exception {
        setupGlusterMock(false, null, true);
        assertTrue(commandMock.canDoAction());
    }

    @Test
    public void canDoActionSucceedsWhenHasPeersThrowsException() throws Exception {
        setupGlusterMock(true, new VDS(), true);
        when(glusterUtil.getPeers(any(EngineSSHClient.class))).thenThrow(new RuntimeException());
        assertTrue(commandMock.canDoAction());
    }

    @Test
    public void canDoActionFailsWhenGlusterServerHasPeers() throws Exception {
        setupGlusterMock(true, new VDS(), true);
        when(glusterDBUtils.serverExists(any(Guid.class), eq(PEER_1))).thenReturn(false);

        assertFalse(commandMock.canDoAction());
        assertTrue(commandMock.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.SERVER_ALREADY_PART_OF_ANOTHER_CLUSTER.toString()));
    }

    @Test
    public void canDoActionSucceedsWhenGlusterServerHasPeersThatExistInDB() throws Exception {
        setupGlusterMock(true, new VDS(), true);
        when(glusterDBUtils.serverExists(any(Guid.class), eq(PEER_1))).thenReturn(true);

        assertTrue(commandMock.canDoAction());
    }

    @Test
    public void canDoActionSucceedsWhenGlusterServerHasNoPeers() throws Exception {
        setupGlusterMock(true, new VDS(), false);
        assertTrue(commandMock.canDoAction());
    }

    @Test
    public void canDoActionSuccessForGlusterServerWhenUpServerExists() throws Exception {
        setupGlusterMock(true, new VDS(), false);
        assertTrue(commandMock.canDoAction());
    }

    @Test
    public void canDoActionFailsForGlusterServerWhenNoUpServer() throws Exception {
        setupGlusterMock(true, null, false);
        assertFalse(commandMock.canDoAction());
        assertTrue(commandMock.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_NO_GLUSTER_HOST_TO_PEER_PROBE.toString()));
    }
}
