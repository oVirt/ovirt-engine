package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Collections;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.hostedengine.HostedEngineHelper;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.RandomUtils;

@RunWith(MockitoJUnitRunner.class)
public class UpdateHostValidatorTest {

    private static final int HOST_NAME_SIZE = 20;
    @Mock
    private DbFacade dbFacade;

    @Mock
    private VdsDao hostDao;

    @Mock
    private ProviderDao providerDao;

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule();

    @Mock
    private VDS host;

    @Mock
    private VDS oldHost;

    @Inject
    private HostedEngineHelper hostedEngineHelper;

    private UpdateHostValidator validator;

    @Before
    public void setup() {
        mockConfigRule.mockConfigValue(ConfigValues.MaxVdsNameLength, HOST_NAME_SIZE);
        validator = new UpdateHostValidator(dbFacade, oldHost, host, false, hostedEngineHelper);
    }

    @Test
    public void hostExists() {
        assertThat(validator.hostExists(), isValid());
    }

    @Test
    public void oldHostDoesNotExist() {
        validator = new UpdateHostValidator(dbFacade, oldHost, null, false, hostedEngineHelper );

        assertThat(validator.hostExists(), failsWith(EngineMessage.VDS_INVALID_SERVER_ID));
    }

    @Test
    public void hostDoesNotExist() {
        validator = new UpdateHostValidator(dbFacade, null, host, false, hostedEngineHelper);
        assertThat(validator.hostExists(), failsWith(EngineMessage.VDS_INVALID_SERVER_ID));
    }

    @Test
    public void hostStatusValid() {
        validator = spy(new UpdateHostValidator(dbFacade, oldHost, host, false, hostedEngineHelper));
        doReturn(true).when(validator).isUpdateValid();

        assertThat(validator.hostStatusValid(), isValid());
    }

    @Test
    public void hostStatusInvalid() {
        validator = spy(new UpdateHostValidator(dbFacade, oldHost, host, false, hostedEngineHelper));
        doReturn(false).when(validator).isUpdateValid();

        assertThat(validator.hostStatusValid(), failsWith(EngineMessage.VDS_STATUS_NOT_VALID_FOR_UPDATE));
    }

    @Test
    public void updateHostAddressAllowed() {
        String hostName = generateRandomName();
        when(oldHost.getHostName()).thenReturn(hostName);
        when(host.getHostName()).thenReturn(hostName);
        when(oldHost.getStatus()).thenReturn(VDSStatus.InstallFailed);

        assertThat(validator.updateHostAddressAllowed(), isValid());
    }

    @Test
    public void updateHostAddressNotAllowedWhenStatusNotInstallFailed() {
        when(oldHost.getHostName()).thenReturn(generateRandomName());
        when(host.getHostName()).thenReturn(generateRandomName());
        when(oldHost.getStatus()).thenReturn(VDSStatus.Maintenance);

        assertThat(validator.updateHostAddressAllowed(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_HOSTNAME_CANNOT_CHANGE));
    }

    @Test
    public void nameNotChanged() {
        String name = generateRandomName();
        when(oldHost.getName()).thenReturn(name);
        when(host.getName()).thenReturn(name);

        assertThat(validator.nameNotUsed(), isValid());
    }

    @Test
    public void nameNotUsed() {
        when(oldHost.getName()).thenReturn(generateRandomName());
        when(host.getName()).thenReturn(generateRandomName());
        when(hostDao.getByName(any(String.class))).thenReturn(null);
        when(dbFacade.getVdsDao()).thenReturn(hostDao);
        validator = new UpdateHostValidator(dbFacade, oldHost, host, false, hostedEngineHelper);

        assertThat(validator.nameNotUsed(), isValid());
    }

    @Test
    public void nameInUse() {
        when(oldHost.getName()).thenReturn(generateRandomName());
        when(host.getName()).thenReturn(generateRandomName());
        when(hostDao.getByName(any(String.class))).thenReturn(mock(VDS.class));
        when(dbFacade.getVdsDao()).thenReturn(hostDao);
        validator = new UpdateHostValidator(dbFacade, oldHost, host, false, hostedEngineHelper);

        assertThat(validator.nameNotUsed(), failsWith(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED));
    }

    @Test
    public void hostNameNotChanged() {
        String name = generateRandomName();
        when(oldHost.getHostName()).thenReturn(name);
        when(host.getHostName()).thenReturn(name);

        assertThat(validator.hostNameNotUsed(), isValid());
    }

    @Test
    public void hostNNotUsed() {
        when(oldHost.getHostName()).thenReturn(generateRandomName());
        when(host.getHostName()).thenReturn(generateRandomName());
        when(hostDao.getAllForHostname(any(String.class))).thenReturn(Collections.<VDS> emptyList());
        when(dbFacade.getVdsDao()).thenReturn(hostDao);
        validator = new UpdateHostValidator(dbFacade, oldHost, host, false, hostedEngineHelper);

        assertThat(validator.hostNameNotUsed(), isValid());
    }

    @Test
    public void hostNInUse() {
        when(oldHost.getHostName()).thenReturn(generateRandomName());
        when(host.getHostName()).thenReturn(generateRandomName());
        when(hostDao.getAllForHostname(any(String.class))).thenReturn(Collections.singletonList(mock(VDS.class)));
        when(dbFacade.getVdsDao()).thenReturn(hostDao);
        validator = new UpdateHostValidator(dbFacade, oldHost, host, false, hostedEngineHelper);

        assertThat(validator.hostNameNotUsed(), failsWith(EngineMessage.ACTION_TYPE_FAILED_VDS_WITH_SAME_HOST_EXIST));
    }

    @Test
    public void anyStatusValidWhenNoInstallationRequired() {
        validator = new UpdateHostValidator(dbFacade, oldHost, host, false, hostedEngineHelper);

        assertThat(validator.statusSupportedForHostInstallation(), isValid());
    }

    @Test
    public void statusSupportedForHostInstallation() {
        when(oldHost.getStatus()).thenReturn(VDSStatus.Maintenance);
        validator = createValidatorForHostInstallation();

        assertThat(validator.statusSupportedForHostInstallation(), isValid());
    }

    @Test
    public void statusNotSupportedForHostInstallation() {
        when(oldHost.getStatus()).thenReturn(VDSStatus.Up);
        validator = createValidatorForHostInstallation();

        assertThat(validator.statusSupportedForHostInstallation(),
                failsWith(EngineMessage.VDS_CANNOT_INSTALL_STATUS_ILLEGAL));
    }

    @Test
    public void passwordNotNeededWhenNoInstallationRequired() {
        assertThat(validator.passwordProvidedForHostInstallation(RandomUtils.instance()
                .nextEnum(AuthenticationMethod.class),
                null), isValid());
    }

    @Test
    public void passwordNotNeededForNonPasswordMethod() {
        validator = createValidatorForHostInstallation();

        assertThat(validator.passwordProvidedForHostInstallation(AuthenticationMethod.PublicKey, null), isValid());
    }

    @Test
    public void passwordProvidedForHostInstallation() {
        when(host.getVdsType()).thenReturn(VDSType.VDS);
        validator = createValidatorForHostInstallation();

        assertThat(validator.passwordProvidedForHostInstallation(AuthenticationMethod.Password, RandomUtils.instance()
                .nextString(10)), isValid());
    }

    @Test
    public void passwordNotProvidedForHostInstallation() {
        when(host.getVdsType()).thenReturn(VDSType.VDS);
        validator = createValidatorForHostInstallation();

        assertThat(validator.passwordProvidedForHostInstallation(AuthenticationMethod.Password, null),
                failsWith(EngineMessage.VDS_CANNOT_INSTALL_EMPTY_PASSWORD));
    }

    @Test
    public void updatePortAllowedWhenInstallationRequired() {
        when(oldHost.getPort()).thenReturn(RandomUtils.instance().nextInt());
        when(host.getPort()).thenReturn(RandomUtils.instance().nextInt());
        validator = createValidatorForHostInstallation();

        assertThat(validator.updatePortAllowed(), isValid());
    }

    @Test
    public void portsNotChanged() {
        int port = RandomUtils.instance().nextInt();
        when(oldHost.getPort()).thenReturn(port);
        when(host.getPort()).thenReturn(port);

        assertThat(validator.updatePortAllowed(), isValid());
    }

    @Test
    public void updatePortNotAllowed() {
        when(oldHost.getPort()).thenReturn(RandomUtils.instance().nextInt());
        when(host.getPort()).thenReturn(RandomUtils.instance().nextInt());

        assertThat(validator.updatePortAllowed(), failsWith(EngineMessage.VDS_PORT_CHANGE_REQUIRE_INSTALL));
    }

    @Test
    public void clusterNotChanged() {
        Guid clusterId = Guid.newGuid();
        when(oldHost.getClusterId()).thenReturn(clusterId);
        when(host.getClusterId()).thenReturn(clusterId);

        assertThat(validator.clusterNotChanged(), isValid());
    }

    @Test
    public void clusterChanged() {
        when(oldHost.getClusterId()).thenReturn(Guid.newGuid());
        when(host.getClusterId()).thenReturn(Guid.newGuid());

        assertThat(validator.clusterNotChanged(), failsWith(EngineMessage.VDS_CANNOT_UPDATE_CLUSTER));
    }

    @Test
    public void protocolNotChanged() {
        VdsProtocol protocol = RandomUtils.instance().nextEnum(VdsProtocol.class);
        when(oldHost.getProtocol()).thenReturn(protocol);
        when(host.getProtocol()).thenReturn(protocol);

        assertThat(validator.changeProtocolAllowed(), isValid());
    }

    @Test
    public void changeProtocolAllowed() {
        when(oldHost.getProtocol()).thenReturn(VdsProtocol.XML);
        when(host.getProtocol()).thenReturn(VdsProtocol.STOMP);
        when(oldHost.getStatus()).thenReturn(VDSStatus.Maintenance);

        assertThat(validator.changeProtocolAllowed(), isValid());
    }

    @Test
    public void changeProtocolNotAllowed() {
        when(oldHost.getProtocol()).thenReturn(VdsProtocol.XML);
        when(host.getProtocol()).thenReturn(VdsProtocol.STOMP);
        when(oldHost.getStatus()).thenReturn(VDSStatus.Up);

        assertThat(validator.changeProtocolAllowed(), failsWith(EngineMessage.VDS_STATUS_NOT_VALID_FOR_UPDATE));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void hostProviderExists() {
        when(host.getHostProviderId()).thenReturn(Guid.newGuid());
        when(providerDao.get(any(Guid.class))).thenReturn(mock(Provider.class));
        when(dbFacade.getProviderDao()).thenReturn(providerDao);
        validator = new UpdateHostValidator(dbFacade, oldHost, host, false, hostedEngineHelper);

        assertThat(validator.hostProviderExists(), isValid());
    }

    @Test
    public void hostProviderDoesNotExist() {
        when(host.getHostProviderId()).thenReturn(Guid.newGuid());
        when(providerDao.get(any(Guid.class))).thenReturn(null);
        when(dbFacade.getProviderDao()).thenReturn(providerDao);
        validator = new UpdateHostValidator(dbFacade, oldHost, host, false, hostedEngineHelper);

        assertThat(validator.hostProviderExists(), failsWith(EngineMessage.ACTION_TYPE_FAILED_PROVIDER_DOESNT_EXIST));
    }

    @Test
    public void hostProviderDoesNotSet() {
        assertThat(validator.hostProviderExists(), isValid());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void hostProviderTypeMatches() {
        when(host.getHostProviderId()).thenReturn(Guid.newGuid());
        Provider provider = mock(Provider.class);
        when(provider.getType()).thenReturn(ProviderType.FOREMAN);
        when(providerDao.get(any(Guid.class))).thenReturn(provider);
        when(dbFacade.getProviderDao()).thenReturn(providerDao);
        validator = new UpdateHostValidator(dbFacade, oldHost, host, false, hostedEngineHelper);

        assertThat(validator.hostProviderTypeMatches(), isValid());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void hostProviderTypeDoesNotMatch() {
        when(host.getHostProviderId()).thenReturn(Guid.newGuid());
        Provider provider = mock(Provider.class);
        when(provider.getType()).thenReturn(ProviderType.OPENSTACK_IMAGE);
        when(providerDao.get(any(Guid.class))).thenReturn(provider);
        when(dbFacade.getProviderDao()).thenReturn(providerDao);
        validator = new UpdateHostValidator(dbFacade, oldHost, host, false, hostedEngineHelper);

        assertThat(validator.hostProviderTypeMatches(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_HOST_PROVIDER_TYPE_MISMATCH));
    }

    private String generateRandomName() {
        return RandomUtils.instance().nextString(HOST_NAME_SIZE);
    }

    private UpdateHostValidator createValidatorForHostInstallation() {
        return new UpdateHostValidator(dbFacade, oldHost, host, true, hostedEngineHelper);
    }
}
