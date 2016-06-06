package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
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
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.utils.RandomUtils;

@RunWith(MockitoJUnitRunner.class)
public class HostValidatorTest {

    private static final int HOST_NAME_SIZE = 20;
    @Mock
    private DbFacade dbFacade;

    @Mock
    private VdsStaticDao hostStaticDao;

    @Mock
    private VdsDao hostDao;

    @Mock
    private StoragePoolDao storagePoolDao;

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule();

    @Mock
    private VDS host;

    @Mock
    private HostedEngineHelper hostedEngineHelper;

    @Mock
    private ClusterDao clusterDao;

    private HostValidator validator;

    private HostValidator mockHostForActivation(VDSStatus status) {
        VDS host = mock(VDS.class);
        when(host.getStatus()).thenReturn(status);
        return new HostValidator(dbFacade, host, hostedEngineHelper);
    }

    private HostValidator mockHostForUniqueId(String value) {
        mockConfigRule.mockConfigValue(ConfigValues.InstallVds, Boolean.TRUE);
        VDS host = mock(VDS.class);
        when(host.getUniqueId()).thenReturn(value);
        return new HostValidator(dbFacade, host, hostedEngineHelper);
    }

    private HostValidator mockHostForProtocol(VdsProtocol protocol) {
        VDS host = mock(VDS.class);
        when(host.getProtocol()).thenReturn(protocol);
        return new HostValidator(dbFacade, host, hostedEngineHelper);
    }

    @Before
    public void setup() {
        mockConfigRule.mockConfigValue(ConfigValues.MaxVdsNameLength, HOST_NAME_SIZE);
        mockConfigRule.mockConfigValue(ConfigValues.EncryptHostCommunication, Boolean.TRUE);
        when(dbFacade.getClusterDao()).thenReturn(clusterDao);
        validator = new HostValidator(dbFacade, host, hostedEngineHelper);
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
        when(hostDao.getByName(any(String.class))).thenReturn(null);
        when(dbFacade.getVdsDao()).thenReturn(hostDao);
        validator = new HostValidator(dbFacade, host, hostedEngineHelper);

        assertThat(validator.nameNotUsed(), isValid());
    }

    @Test
    public void nameIsUsed() {
        when(hostDao.getByName(any(String.class))).thenReturn(mock(VDS.class));
        when(dbFacade.getVdsDao()).thenReturn(hostDao);
        validator = new HostValidator(dbFacade, host, hostedEngineHelper);

        assertThat(validator.nameNotUsed(), failsWith(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED));
    }

    @Test
    public void hostNameNotUsed() {
        when(hostDao.getAllForHostname(any(String.class))).thenReturn(Collections.<VDS> emptyList());
        when(dbFacade.getVdsDao()).thenReturn(hostDao);
        validator = new HostValidator(dbFacade, host, hostedEngineHelper);

        assertThat(validator.hostNameNotUsed(), isValid());
    }

    @Test
    public void hostNameIsUsed() {
        when(hostDao.getAllForHostname(any(String.class))).thenReturn(Collections.singletonList(mock(VDS.class)));
        when(dbFacade.getVdsDao()).thenReturn(hostDao);
        validator = new HostValidator(dbFacade, host, hostedEngineHelper);

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
        when(storagePoolDao.getForCluster(any(Guid.class))).thenReturn(null);
        when(dbFacade.getStoragePoolDao()).thenReturn(storagePoolDao);
        validator = new HostValidator(dbFacade, host, hostedEngineHelper);

        assertThat(validator.validateSingleHostAttachedToLocalStorage(), isValid());
    }

    @Test
    public void validateSingleHostAttachedToLocalStorage() {
        StoragePool dataCenter = mock(StoragePool.class);
        when(dataCenter.isLocal()).thenReturn(true);
        when(storagePoolDao.getForCluster(any(Guid.class))).thenReturn(dataCenter);
        when(dbFacade.getStoragePoolDao()).thenReturn(storagePoolDao);
        when(hostStaticDao.getAllForCluster(any(Guid.class))).thenReturn(Collections.<VdsStatic> emptyList());
        when(dbFacade.getVdsStaticDao()).thenReturn(hostStaticDao);
        validator = new HostValidator(dbFacade, host, hostedEngineHelper);

        assertThat(validator.validateSingleHostAttachedToLocalStorage(), isValid());
    }

    @Test
    public void validateSingleHostAttachedToFewStorages() {
        StoragePool dataCenter = mock(StoragePool.class);
        when(dataCenter.isLocal()).thenReturn(true);
        when(storagePoolDao.getForCluster(any(Guid.class))).thenReturn(dataCenter);
        when(dbFacade.getStoragePoolDao()).thenReturn(storagePoolDao);
        when(hostStaticDao.getAllForCluster(any(Guid.class)))
                .thenReturn(Collections.<VdsStatic> singletonList(mock(VdsStatic.class)));
        when(dbFacade.getVdsStaticDao()).thenReturn(hostStaticDao);
        validator = new HostValidator(dbFacade, host, hostedEngineHelper);

        assertThat(validator.validateSingleHostAttachedToLocalStorage(),
                failsWith(EngineMessage.VDS_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE));
    }

    @Test
    public void securityKeysExists() {
        validator = spy(new HostValidator(dbFacade, host, hostedEngineHelper));
        doReturn(true).when(validator).haveSecurityKey();

        assertThat(validator.securityKeysExists(), isValid());
    }

    @Test
    public void securityKeysDoesNotExist() {
        validator = spy(new HostValidator(dbFacade, host, hostedEngineHelper));
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
        validator = mockHostForActivation(VDSStatus.Up);
        assertThat(validator.validateStatusForActivation(), failsWith( EngineMessage.VDS_ALREADY_UP));
    }

    @Test
    public void testValidateStatusNonResponsiveForActivation() {
        validator = mockHostForActivation(VDSStatus.NonResponsive);
        assertThat(validator.validateStatusForActivation(), failsWith( EngineMessage.VDS_NON_RESPONSIVE));
    }

    @Test
    public void testValidateNoUniqueId() {
        validator = mockHostForUniqueId(StringUtils.EMPTY);
        assertThat(validator.validateUniqueId(), failsWith( EngineMessage.VDS_NO_UUID));
    }

    @Test
    public void testValidateUniqueId() {
        validator = mockHostForUniqueId(Guid.newGuid().toString());
        assertThat(validator.validateUniqueId(), isValid());
    }

    @Test
    public void testIsNotUp() {
        validator = mockHostForActivation(VDSStatus.Down);
        assertNotEquals(validator.isUp().getMessage(), EngineMessage.VAR__HOST_STATUS__UP);
    }

    @Test
    public void testIsUp() {
        validator = mockHostForActivation(VDSStatus.Up);
        assertTrue(validator.isUp().isValid());
    }

    @Test
    public void testValidStatusForEnrollCertificate() {
        validator = mockHostForActivation(VDSStatus.Maintenance);
        assertThat(validator.validateStatusForEnrollCertificate(), isValid());
    }

    @Test
    public void testInvalidStatusForEnrollCertificate() {
        validator = mockHostForActivation(VDSStatus.Up);
        assertThat(validator.validateStatusForEnrollCertificate(),
                failsWith(EngineMessage.CANNOT_ENROLL_CERTIFICATE_HOST_STATUS_ILLEGAL));
    }

    @Test
    public void testValidateXmlProtocolForCluster() {
        validator = mockHostForProtocol(VdsProtocol.XML);
        assertThat(validator.protocolIsNotXmlrpc(),
                failsWith(EngineMessage.NOT_SUPPORTED_PROTOCOL_FOR_CLUSTER_VERSION));
    }

    @Test
    public void supportsHostedEngineDeploy() {
        when(hostedEngineHelper.isVmManaged()).thenReturn(true);
        mockCluster(Version.v4_0);
        assertThat(validator.supportsDeployingHostedEngine(new HostedEngineDeployConfiguration(HostedEngineDeployConfiguration.Action.DEPLOY)),
                isValid());
    }

    @Test
    public void unsupportedHostedEngineDeployWhenNoHostedEngine() {
        when(hostedEngineHelper.isVmManaged()).thenReturn(false);
        mockCluster(Version.v4_0);
        when(host.getClusterId()).thenReturn(Guid.Empty);
        assertThat(validator.supportsDeployingHostedEngine(new HostedEngineDeployConfiguration(HostedEngineDeployConfiguration.Action.DEPLOY)),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_UNMANAGED_HOSTED_ENGINE));
    }

    @Test
    public void unsupportedHostedEngineDeployWhenClusterLevelIsUnsupported() {
        when(hostedEngineHelper.isVmManaged()).thenReturn(false);
        mockCluster(Version.v3_6);
        when(host.getClusterId()).thenReturn(Guid.Empty);
        assertThat(validator.supportsDeployingHostedEngine(new HostedEngineDeployConfiguration(HostedEngineDeployConfiguration.Action.DEPLOY)),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_HOSTED_ENGINE_DEPLOYMENT_UNSUPPORTED));
    }

    @Test
    public void allow36HostWithoutDeployingHostedEngine() {
        when(hostedEngineHelper.isVmManaged()).thenReturn(false);
        mockCluster(Version.v3_6);
        when(host.getClusterId()).thenReturn(Guid.Empty);
        HostedEngineDeployConfiguration heConfig =
                new HostedEngineDeployConfiguration(HostedEngineDeployConfiguration.Action.NONE);
        assertThat(validator.supportsDeployingHostedEngine(heConfig), isValid());
    }

    public void mockCluster(Version version) {
        Cluster cluster = new Cluster();
        cluster.setCompatibilityVersion(version);
        when(clusterDao.get(any(Guid.class))).thenReturn(cluster);
    }
}
