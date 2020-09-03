package org.ovirt.engine.core.bll.hostdeploy;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.bll.validator.AffinityValidator;
import org.ovirt.engine.core.bll.validator.UpdateHostValidator;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.action.hostdeploy.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration;
import org.ovirt.engine.core.common.businessentities.ReplaceHostConfiguration;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.FenceAgentDao;
import org.ovirt.engine.core.dao.VdsDao;

public class UpdateVdsCommandTest {
    private static final Guid HOST_ID = Guid.newGuid();
    private static final Guid CLUSTER_ID = Guid.newGuid();
    private static final Version CLUSTER_VERSION = new Version("1.2.3");
    private static final String PASSWORD = "password";
    private static final HostedEngineDeployConfiguration HOSTED_ENGINE_DEPLOY_CONFIGURATION =
            new HostedEngineDeployConfiguration();
    private static final ReplaceHostConfiguration REPLACE_HOST_CONFIGURATION =
            new ReplaceHostConfiguration();


    @Mock
    private VdsDao vdsDaoMock;

    @Mock
    private FenceAgentDao fenceAgentDao;

    @Mock
    private UpdateHostValidator updateHostValidator;

    @Mock
    private AffinityValidator affinityValidator;

    @Mock
    private Cluster cluster;

    @InjectMocks
    private UpdateVdsCommand<UpdateVdsActionParameters> underTestCommand;

    private VDS newHost;
    private VDS oldHost;
    private UpdateVdsActionParameters parameters;

    @BeforeEach
    public void setUp() {
        newHost = createTestHost(HOST_ID);
        parameters = createParameters(newHost);
        underTestCommand = new UpdateVdsCommand<>(parameters, null);
        underTestCommand.setClusterId(newHost.getClusterId());

        MockitoAnnotations.initMocks(this);
        underTestCommand = spy(underTestCommand);

        oldHost = createOldHost(newHost);
        doReturn(updateHostValidator)
                .when(underTestCommand)
                .getUpdateHostValidator(oldHost, parameters.getvds(), parameters.isInstallHost());

        when(affinityValidator.validateAffinityUpdateForHost(any(), any(), any(), any()))
                .thenReturn(AffinityValidator.Result.VALID);
    }

    @Test
    public void testValidate() {
        when(vdsDaoMock.get(HOST_ID)).thenReturn(oldHost);
        when(fenceAgentDao.getFenceAgentsForHost(HOST_ID)).thenReturn(new ArrayList());
        assertTrue(underTestCommand.validate());
    }

    private VDS createOldHost(VDS host) {
        VDS oldHost = host.clone();
        oldHost.setVdsName("FOO");
        return oldHost;
    }

    private VDS createTestHost(Guid hostId) {
        VDS host = new VDS();
        host.setHostName("BUZZ");
        host.setVdsName("BAR");
        host.setClusterCompatibilityVersion(CLUSTER_VERSION);
        host.setClusterId(CLUSTER_ID);
        host.setId(hostId);
        return host;
    }

    private UpdateVdsActionParameters createParameters(VDS host) {
        UpdateVdsActionParameters parameters = new UpdateVdsActionParameters();
        parameters.setvds(host);
        parameters.setVdsId(host.getId());
        parameters.setAuthMethod(AuthenticationMethod.PublicKey);
        parameters.setPassword(PASSWORD);
        parameters.setHostedEngineDeployConfiguration(HOSTED_ENGINE_DEPLOY_CONFIGURATION);
        return parameters;
    }
}
