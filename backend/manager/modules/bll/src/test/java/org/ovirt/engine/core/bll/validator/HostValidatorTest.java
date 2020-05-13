package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Collections;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.hostedengine.HostedEngineHelper;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.ExternalComputeResource;
import org.ovirt.engine.core.common.businessentities.ExternalHostGroup;
import org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.RandomUtils;

@ExtendWith({ MockitoExtension.class, MockConfigExtension.class })
@MockitoSettings(strictness = Strictness.LENIENT)
public class HostValidatorTest {

    private static final int HOST_NAME_SIZE = 20;
    private static final Guid DC_ID = new Guid("35822eef-2e7f-4d1a-bfd8-12e754ef7ef9");

    @Mock
    private VdsStaticDao hostStaticDao;

    @Mock
    private VdsDao hostDao;

    @Mock
    private StoragePoolDao storagePoolDao;
    @Mock
    private Cluster cluster;
    @Mock
    private VDS host;
    @Mock
    private HostedEngineHelper hostedEngineHelper;
    @Spy
    @InjectMocks
    private HostValidator validator = new HostValidator(host);

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.EncryptHostCommunication, true),
                MockConfigDescriptor.of(ConfigValues.InstallVds, Boolean.TRUE),
                MockConfigDescriptor.of(ConfigValues.MaxVdsNameLength, HOST_NAME_SIZE));
    }

    @BeforeEach
    private void setUp() {
        when(cluster.getStoragePoolId()).thenReturn(DC_ID);
        when(host.getStoragePoolId()).thenReturn(Guid.Empty);
        when(hostedEngineHelper.getStoragePoolId()).thenReturn(DC_ID);
    }

    private void mockHostForActivation(VDSStatus status) {
        when(host.getStatus()).thenReturn(status);
    }

    @Test
    public void nameIsNull() {
        assertThat(validator.nameNotEmpty(), failsWith(EngineMessage.ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY));
    }

    @Test
    public void nameIsEmpty() {
        when(host.getName()).thenReturn("");
        assertThat(validator.nameNotEmpty(), failsWith(EngineMessage.ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY));
    }

    @Test
    public void nameNotEmpty() {
        when(host.getName()).thenReturn(RandomUtils.instance().nextString(HOST_NAME_SIZE));
        assertThat(validator.nameNotEmpty(), isValid());
    }

    @Test
    public void nameLengthIsTooLong() {
        when(host.getName()).thenReturn(RandomUtils.instance().nextString(HOST_NAME_SIZE * 2));
        assertThat(validator.nameLengthIsLegal(), failsWith(EngineMessage.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG));
    }

    @Test
    public void hostNameIsValid() {
        when(host.getHostName()).thenReturn("validName");
        assertThat(validator.hostNameIsValid(), isValid());
    }

    @Test
    public void hostNameIsInvalid() {
        assertThat(validator.hostNameIsValid(), failsWith(EngineMessage.ACTION_TYPE_FAILED_INVALID_VDS_HOSTNAME));
    }

    @Test
    public void nameNotUsed() {
        assertThat(validator.nameNotUsed(), isValid());
    }

    @Test
    public void nameIsUsed() {
        when(hostDao.getByName(any(), any())).thenReturn(mock(VDS.class));

        assertThat(validator.nameNotUsed(), failsWith(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED));
    }

    @Test
    public void hostNameNotUsed() {
        assertThat(validator.hostNameNotUsed(), isValid());
    }

    @Test
    public void hostNameIsUsed() {
        when(hostDao.getAllForHostname(any(), any())).thenReturn(Collections.singletonList(mock(VDS.class)));

        assertThat(validator.hostNameNotUsed(), failsWith(EngineMessage.ACTION_TYPE_FAILED_VDS_WITH_SAME_HOST_EXIST));
    }

    @Test
    public void portIsValid() {
        when(host.getSshPort()).thenReturn(BusinessEntitiesDefinitions.NETWORK_MIN_LEGAL_PORT);
        assertThat(validator.portIsValid(), isValid());
    }

    @Test
    public void portIsInvalid() {
        assertThat(validator.portIsValid(), failsWith(EngineMessage.ACTION_TYPE_FAILED_VDS_WITH_INVALID_SSH_PORT));
    }

    @Test
    public void sshUserNameNotEmpty() {
        when(host.getSshUsername()).thenReturn(RandomUtils.instance().nextString(HOST_NAME_SIZE));
        assertThat(validator.sshUserNameNotEmpty(), isValid());
    }

    @Test
    public void sshUserNameIsEmpty() {
        assertThat(validator.sshUserNameNotEmpty(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_VDS_WITH_INVALID_SSH_USERNAME));
    }

    @Test
    public void hostAttachedToLocalStorageWithoutDataCenter() {
        assertThat(validator.validateSingleHostAttachedToLocalStorage(), isValid());
    }

    @Test
    public void validateSingleHostAttachedToLocalStorage() {
        assertThat(validator.validateSingleHostAttachedToLocalStorage(), isValid());
    }

    @Test
    public void validateSingleHostAttachedToFewStorages() {
        StoragePool dataCenter = mock(StoragePool.class);
        when(dataCenter.isLocal()).thenReturn(true);
        when(storagePoolDao.getForCluster(any())).thenReturn(dataCenter);
        when(hostStaticDao.getAllForCluster(any()))
                .thenReturn(Collections.singletonList(mock(VdsStatic.class)));

        assertThat(validator.validateSingleHostAttachedToLocalStorage(),
                failsWith(EngineMessage.VDS_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE));
    }

    @Test
    public void securityKeysExists() {
        doReturn(true).when(validator).haveSecurityKey();

        assertThat(validator.securityKeysExists(), isValid());
    }

    @Test
    public void securityKeysDoesNotExist() {
        doReturn(false).when(validator).haveSecurityKey();

        assertThat(validator.securityKeysExists(),
                failsWith(EngineMessage.VDS_TRY_CREATE_SECURE_CERTIFICATE_NOT_FOUND));
    }

    @Test
    public void passwordNotEmpty() {
        assertThat(validator.passwordNotEmpty(false,
                AuthenticationMethod.Password,
                RandomUtils.instance().nextString(10)),
                isValid());
    }

    @Test
    public void passwordIsEmpty() {
        assertThat(validator.passwordNotEmpty(false, AuthenticationMethod.Password, null),
                failsWith(EngineMessage.VDS_CANNOT_INSTALL_EMPTY_PASSWORD));
    }

    @Test
    public void passwordIsEmptyWhenUsingPublicKey() {
        assertThat(validator.passwordNotEmpty(false, AuthenticationMethod.PublicKey, null), isValid());
    }

    @Test
    public void passwordIsEmptyForOvirtNode() {
        assertThat(validator.passwordNotEmpty(true, RandomUtils.instance().nextEnum(AuthenticationMethod.class), null),
                isValid());
    }

    @Test
    public void provisioningComputeResourceEmpty() {
        assertThat(validator.provisioningComputeResourceValid(true, null),
                failsWith(EngineMessage.VDS_PROVIDER_PROVISION_MISSING_COMPUTERESOURCE));
    }

    @Test
    public void provisioningHostGroupEmpty() {
        assertThat(validator.provisioningHostGroupValid(true, null),
                failsWith(EngineMessage.VDS_PROVIDER_PROVISION_MISSING_HOSTGROUP));
    }

    @Test
    public void provisioningNotProvided() {
        assertThat(validator.provisioningComputeResourceValid(false, null), isValid());
        assertThat(validator.provisioningHostGroupValid(false, null), isValid());
    }

    @Test
    public void provisioningProvided() {
        assertThat(validator.provisioningComputeResourceValid(false, new ExternalComputeResource()), isValid());
        assertThat(validator.provisioningHostGroupValid(false, new ExternalHostGroup()), isValid());
        assertThat(validator.provisioningComputeResourceValid(true, new ExternalComputeResource()), isValid());
        assertThat(validator.provisioningHostGroupValid(true, new ExternalHostGroup()), isValid());
    }

    @Test
    public void testValidateStatusUpForActivation() {
        mockHostForActivation(VDSStatus.Up);
        assertThat(validator.validateStatusForActivation(), failsWith(EngineMessage.VDS_ALREADY_UP));
    }

    @Test
    public void testValidateStatusNonResponsiveForActivation() {
        mockHostForActivation(VDSStatus.NonResponsive);
        assertThat(validator.validateStatusForActivation(), failsWith(EngineMessage.VDS_NON_RESPONSIVE));
    }

    @Test
    public void testValidateNoUniqueId() {
        when(host.getUniqueId()).thenReturn(StringUtils.EMPTY);
        assertThat(validator.validateUniqueId(), failsWith(EngineMessage.VDS_NO_UUID));
    }

    @Test
    public void testValidateUniqueId() {
        when(host.getUniqueId()).thenReturn(Guid.newGuid().toString());
        assertThat(validator.validateUniqueId(), isValid());
    }

    @Test
    public void testIsNotUp() {
        mockHostForActivation(VDSStatus.Down);
        assertThat(validator.isUp(), failsWith(EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL, "$hostStatus Up"));
    }

    @Test
    public void testIsUp() {
        mockHostForActivation(VDSStatus.Up);
        assertTrue(validator.isUp().isValid());
    }

    @Test
    public void testValidStatusForEnrollCertificate() {
        mockHostForActivation(VDSStatus.Maintenance);
        assertThat(validator.validateStatusForEnrollCertificate(), isValid());
    }

    @Test
    public void testInvalidStatusForEnrollCertificate() {
        mockHostForActivation(VDSStatus.Up);
        assertThat(validator.validateStatusForEnrollCertificate(),
                failsWith(EngineMessage.CANNOT_ENROLL_CERTIFICATE_HOST_STATUS_ILLEGAL));
    }

    @Test
    public void supportsHostedEngineDeploy() {
        when(hostedEngineHelper.isVmManaged()).thenReturn(true);
        assertThat(
                validator.supportsDeployingHostedEngine(
                        new HostedEngineDeployConfiguration(HostedEngineDeployConfiguration.Action.DEPLOY),
                        cluster,
                        false),
                isValid());
    }

    @Test
    public void supportsHostedEngineDeployInVersion36() {
        when(hostedEngineHelper.isVmManaged()).thenReturn(true);
        assertThat(
                validator.supportsDeployingHostedEngine(
                        new HostedEngineDeployConfiguration(HostedEngineDeployConfiguration.Action.DEPLOY),
                        cluster,
                        false),
                isValid());
    }

    @Test
    public void unsupportedHostedEngineDeployWhenNoHostedEngine() {
        when(hostedEngineHelper.isVmManaged()).thenReturn(false);
        assertThat(
                validator.supportsDeployingHostedEngine(
                        new HostedEngineDeployConfiguration(HostedEngineDeployConfiguration.Action.DEPLOY),
                        cluster,
                        false),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_UNMANAGED_HOSTED_ENGINE));
    }

    @Test
    public void unsupportedHostedEngineDeployWhenHostInAnotherDC() {
        when(hostedEngineHelper.isVmManaged()).thenReturn(true);
        when(hostedEngineHelper.getStoragePoolId()).thenReturn(Guid.newGuid());
        assertThat(
                validator.supportsDeployingHostedEngine(
                        new HostedEngineDeployConfiguration(HostedEngineDeployConfiguration.Action.DEPLOY),
                        cluster,
                        false),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_HOSTED_ENGINE_HOST_IN_ANOTHER_DC));
    }

    @Test
    public void unsupportedHostedEngineDeployWhenHostInAnotherDCAndHEAlreadyDeployed() {
        when(hostedEngineHelper.isVmManaged()).thenReturn(true);
        when(hostedEngineHelper.getStoragePoolId()).thenReturn(Guid.newGuid());
        assertThat(
                validator.supportsDeployingHostedEngine(null, cluster, true),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_HOSTED_ENGINE_HOST_IN_ANOTHER_DC));
    }

    @Test
    public void allow36HostWithoutDeployingHostedEngine() {
        HostedEngineDeployConfiguration heConfig =
                new HostedEngineDeployConfiguration(HostedEngineDeployConfiguration.Action.NONE);
        assertThat(validator.supportsDeployingHostedEngine(heConfig, cluster, false), isValid());
    }
}
