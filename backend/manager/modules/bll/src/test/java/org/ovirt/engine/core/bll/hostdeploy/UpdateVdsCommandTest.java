package org.ovirt.engine.core.bll.hostdeploy;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.UpdateHostValidator;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.action.hostdeploy.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VdsDao;

public class UpdateVdsCommandTest {
    private static final Guid HOST_ID = Guid.newGuid();
    private static final Guid CLUSTER_ID = Guid.newGuid();
    private static final Version CLUSTER_VERSION = new Version("1.2.3");
    private static final String PASSWORD = "password";
    private static final HostedEngineDeployConfiguration HOSTED_ENGINE_DEPLOY_CONFIGURATION =
            new HostedEngineDeployConfiguration();

    @Mock
    private VdsDao vdsDaoMock;

    @Mock
    private UpdateHostValidator updateHostValidator;

    @InjectMocks
    private UpdateVdsCommand<UpdateVdsActionParameters> underTestCommand;

    private VDS newHost;
    private VDS oldHost;
    private UpdateVdsActionParameters parameters;

    @Before
    public void setUp() {
        newHost = createTestHost(HOST_ID);
        parameters = createParameters(newHost);
        underTestCommand = new UpdateVdsCommand<>(parameters, null);
        MockitoAnnotations.initMocks(this);
        underTestCommand = spy(underTestCommand);

        oldHost = createOldHost(newHost);
        doReturn(updateHostValidator)
                .when(underTestCommand)
                .getUpdateHostValidator(oldHost, parameters.getvds(), parameters.isInstallHost());
    }

    @Test
    public void testValidate() {
        when(vdsDaoMock.get(HOST_ID)).thenReturn(oldHost);

        when(updateHostValidator.hostExists()).thenReturn(ValidationResult.VALID);
        when(updateHostValidator.hostStatusValid()).thenReturn(ValidationResult.VALID);
        when(updateHostValidator.nameNotEmpty()).thenReturn(ValidationResult.VALID);
        when(updateHostValidator.nameLengthIsLegal()).thenReturn(ValidationResult.VALID);
        when(updateHostValidator.updateHostAddressAllowed()).thenReturn(ValidationResult.VALID);
        when(updateHostValidator.nameNotUsed()).thenReturn(ValidationResult.VALID);
        when(updateHostValidator.hostNameNotUsed()).thenReturn(ValidationResult.VALID);
        when(updateHostValidator.statusSupportedForHostInstallation()).thenReturn(ValidationResult.VALID);
        when(updateHostValidator.passwordProvidedForHostInstallation(AuthenticationMethod.PublicKey, PASSWORD))
                .thenReturn(ValidationResult.VALID);
        when(updateHostValidator.updatePortAllowed()).thenReturn(ValidationResult.VALID);
        when(updateHostValidator.clusterNotChanged()).thenReturn(ValidationResult.VALID);
        when(updateHostValidator.hostProviderExists()).thenReturn(ValidationResult.VALID);
        when(updateHostValidator.hostProviderTypeMatches()).thenReturn(ValidationResult.VALID);
        when(updateHostValidator.supportsDeployingHostedEngine(same(HOSTED_ENGINE_DEPLOY_CONFIGURATION)))
                .thenReturn(ValidationResult.VALID);

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
