package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.RandomUtils;

@ExtendWith(MockitoExtension.class)
public class UpdateHostValidatorTest {

    private static final int HOST_NAME_SIZE = 20;

    @Mock
    private VdsDao hostDao;

    @Mock
    private ProviderDao providerDao;

    private VDS host = mock(VDS.class);

    private VDS oldHost = mock(VDS.class);

    @Spy
    @InjectMocks
    private UpdateHostValidator validator = new UpdateHostValidator(oldHost, host, false);

    @Test
    public void hostExists() {
        assertThat(validator.hostExists(), isValid());
    }

    @Test
    public void oldHostDoesNotExist() {
        validator = new UpdateHostValidator(oldHost, null, false);

        assertThat(validator.hostExists(), failsWith(EngineMessage.VDS_INVALID_SERVER_ID));
    }

    @Test
    public void hostDoesNotExist() {
        validator = new UpdateHostValidator(null, host, false);
        assertThat(validator.hostExists(), failsWith(EngineMessage.VDS_INVALID_SERVER_ID));
    }

    @Test
    public void hostStatusValid() {
        doReturn(true).when(validator).isUpdateValid();

        assertThat(validator.hostStatusValid(), isValid());
    }

    @Test
    public void hostStatusInvalid() {
        doReturn(false).when(validator).isUpdateValid();

        assertThat(validator.hostStatusValid(), failsWith(EngineMessage.VDS_STATUS_NOT_VALID_FOR_UPDATE));
    }

    @Test
    public void updateHostAddressAllowed() {
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

        assertThat(validator.nameNotUsed(), isValid());
    }

    @Test
    public void nameInUse() {
        when(oldHost.getName()).thenReturn(generateRandomName());
        when(host.getName()).thenReturn(generateRandomName());
        when(hostDao.getByName(any(), any())).thenReturn(mock(VDS.class));

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

        assertThat(validator.hostNameNotUsed(), isValid());
    }

    @Test
    public void hostNInUse() {
        when(oldHost.getHostName()).thenReturn(generateRandomName());
        when(host.getHostName()).thenReturn(generateRandomName());
        when(hostDao.getAllForHostname(any(), any())).thenReturn(Collections.singletonList(mock(VDS.class)));

        assertThat(validator.hostNameNotUsed(), failsWith(EngineMessage.ACTION_TYPE_FAILED_VDS_WITH_SAME_HOST_EXIST));
    }

    @Test
    public void anyStatusValidWhenNoInstallationRequired() {
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

    @SuppressWarnings("unchecked")
    @Test
    public void hostProviderExists() {
        when(host.getHostProviderId()).thenReturn(Guid.newGuid());
        when(providerDao.get(any())).thenReturn(mock(Provider.class));

        assertThat(validator.hostProviderExists(), isValid());
    }

    @Test
    public void hostProviderDoesNotExist() {
        when(host.getHostProviderId()).thenReturn(Guid.newGuid());

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
        when(providerDao.get(any())).thenReturn(provider);

        assertThat(validator.hostProviderTypeMatches(), isValid());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void hostProviderTypeDoesNotMatch() {
        when(host.getHostProviderId()).thenReturn(Guid.newGuid());
        Provider provider = mock(Provider.class);
        when(provider.getType()).thenReturn(ProviderType.OPENSTACK_IMAGE);
        when(providerDao.get(any())).thenReturn(provider);

        assertThat(validator.hostProviderTypeMatches(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_HOST_PROVIDER_TYPE_MISMATCH));
    }

    private String generateRandomName() {
        return RandomUtils.instance().nextString(HOST_NAME_SIZE);
    }

    private UpdateHostValidator createValidatorForHostInstallation() {
        return new UpdateHostValidator(oldHost, host, true);
    }
}
