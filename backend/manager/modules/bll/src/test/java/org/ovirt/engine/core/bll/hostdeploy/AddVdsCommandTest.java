package org.ovirt.engine.core.bll.hostdeploy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.EngineSSHClient;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.bll.validator.AffinityValidator;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.action.hostdeploy.AddVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.gluster.GlusterDBUtils;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class AddVdsCommandTest {
    private static final String PEER_1 = "peer1";
    private static final Guid vdsId = Guid.newGuid();

    @Spy
    @InjectMocks
    private AddVdsCommand<AddVdsActionParameters> command = new AddVdsCommand<>(createParameters(), null);

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
    private HostValidator validator;
    @Mock
    private AffinityValidator affinityValidator;

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

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.InstallVds, true));
    }

    public AddVdsActionParameters createParameters() {
        AddVdsActionParameters parameters = new AddVdsActionParameters();
        parameters.setPassword("secret");
        VDS newVds = makeTestVds(vdsId);
        parameters.setvds(newVds);
        return parameters;
    }

    private void setupCommonMock(boolean glusterEnabled) throws Exception {
        mockHostValidator();

        doReturn(glusterEnabled).when(command).isGlusterSupportEnabled();

        doReturn(true).when(command).validateCluster();
        doReturn(sshClient).when(command).getSSHClient();
        Version version = new Version("1.2.3");
        Cluster cluster = new Cluster();
        cluster.setCompatibilityVersion(version);
        doReturn(cluster).when(command).getCluster();

        when(affinityValidator.validateAffinityUpdateForHost(any(), any(), any(), any()))
                .thenReturn(AffinityValidator.Result.VALID);
    }

    private void mockHostValidator() {
        doReturn(validator).when(command).getHostValidator();
    }

    private void setupVirtMock() throws Exception {
        setupCommonMock(false);
    }

    private void setupGlusterMock(boolean clusterHasServers, VDS upServer, boolean hasPeers) throws Exception {
        setupCommonMock(true);

        when(glusterUtil.getPeers(any())).thenReturn(hasPeers ? Collections.singleton(PEER_1) : Collections.emptySet());

        when(clusterUtils.hasServers(any())).thenReturn(clusterHasServers);
        when(vdsDaoMock.getAllForCluster(any())).thenReturn(mockVdsInDb(clusterHasServers ? VDSStatus.Maintenance
                : VDSStatus.Initializing));
        when(glusterUtil.getUpServer(any())).thenReturn(upServer);
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
        assertTrue(command.validate());
    }

    @Test
    public void validateSucceedsOnEmptyClusterEvenWhenGlusterServerHasPeers() throws Exception {
        setupGlusterMock(false, null, true);
        assertTrue(command.validate());
    }

    @Test
    public void validateSucceedsWhenHasPeersThrowsException() throws Exception {
        setupGlusterMock(true, new VDS(), true);
        when(glusterUtil.getPeers(any())).thenThrow(new RuntimeException());
        assertTrue(command.validate());
    }

    @Test
    public void validateFailsWhenGlusterServerHasPeers() throws Exception {
        setupGlusterMock(true, new VDS(), true);
        when(glusterDBUtils.serverExists(any(), eq(PEER_1))).thenReturn(false);

        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.SERVER_ALREADY_PART_OF_ANOTHER_CLUSTER.toString()));
    }

    @Test
    public void validateSucceedsWhenGlusterServerHasPeersThatExistInDB() throws Exception {
        setupGlusterMock(true, new VDS(), true);
        when(glusterDBUtils.serverExists(any(), eq(PEER_1))).thenReturn(true);

        assertTrue(command.validate());
    }

    @Test
    public void validateSucceedsWhenGlusterServerHasNoPeers() throws Exception {
        setupGlusterMock(true, new VDS(), false);
        assertTrue(command.validate());
    }

    @Test
    public void validateSuccessForGlusterServerWhenUpServerExists() throws Exception {
        setupGlusterMock(true, new VDS(), false);
        assertTrue(command.validate());
    }

    @Test
    public void validateFailsForGlusterServerWhenNoUpServer() throws Exception {
        setupGlusterMock(true, null, false);
        assertFalse(command.validate());
        assertTrue(command.getReturnValue()
                .getValidationMessages()
                .contains(EngineMessage.ACTION_TYPE_FAILED_NO_GLUSTER_HOST_TO_PEER_PROBE.toString()));
    }

    @Test
    public void provisioningValidated() throws Exception {
        setupVirtMock();
        assertTrue(command.validate());
        verify(validator, times(1)).provisioningComputeResourceValid(anyBoolean(), any());
        verify(validator, times(1)).provisioningHostGroupValid(anyBoolean(), any());
    }

}
