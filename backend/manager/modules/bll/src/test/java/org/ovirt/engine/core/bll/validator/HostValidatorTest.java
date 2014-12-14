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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsStaticDAO;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;

@RunWith(MockitoJUnitRunner.class)
public class HostValidatorTest {

    private final static int HOST_NAME_SIZE = 20;
    @Mock
    private DbFacade dbFacade;

    @Mock
    private VdsStaticDAO hostStaticDao;

    @Mock
    private VdsDAO hostDao;

    @Mock
    private StoragePoolDAO storagePoolDao;

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule();

    @Mock
    private VDS host;

    private HostValidator validator;

    @Before
    public void setup() {
        mockConfigRule.mockConfigValue(ConfigValues.MaxVdsNameLength, HOST_NAME_SIZE);
        mockConfigRule.mockConfigValue(ConfigValues.EncryptHostCommunication, Boolean.TRUE);
        validator = new HostValidator(dbFacade, host);
    }

    @Test
    public void nameIsNull() {
        assertThat(validator.nameNotEmpty(), failsWith(VdcBllMessages.ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY));
    }

    @Test
    public void nameIsEmpty() {
        when(host.getName()).thenReturn("");
        assertThat(validator.nameNotEmpty(), failsWith(VdcBllMessages.ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY));
    }

    @Test
    public void nameNotEmpty() {
        when(host.getName()).thenReturn(RandomUtils.instance().nextString(HOST_NAME_SIZE));
        assertThat(validator.nameNotEmpty(), isValid());
    }

    @Test
    public void NameLengthIsTooLong() {
        when(host.getName()).thenReturn(RandomUtils.instance().nextString(HOST_NAME_SIZE * 2));
        assertThat(validator.nameLengthIsLegal(), failsWith(VdcBllMessages.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG));
    }

    @Test
    public void hostNameIsValid() {
        when(host.getHostName()).thenReturn("validName");
        assertThat(validator.hostNameIsValid(), isValid());
    }

    @Test
    public void hostNameIsInvalid() {
        assertThat(validator.hostNameIsValid(), failsWith(VdcBllMessages.ACTION_TYPE_FAILED_INVALID_VDS_HOSTNAME));
    }

    @Test
    public void nameNotUsed() {
        when(hostDao.getByName(any(String.class))).thenReturn(null);
        when(dbFacade.getVdsDao()).thenReturn(hostDao);
        validator = new HostValidator(dbFacade, host);

        assertThat(validator.nameNotUsed(), isValid());
    }

    @Test
    public void nameIsUsed() {
        when(hostDao.getByName(any(String.class))).thenReturn(mock(VDS.class));
        when(dbFacade.getVdsDao()).thenReturn(hostDao);
        validator = new HostValidator(dbFacade, host);

        assertThat(validator.nameNotUsed(), failsWith(VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED));
    }

    @Test
    public void hostNameNotUsed() {
        when(hostDao.getAllForHostname(any(String.class))).thenReturn(Collections.<VDS> emptyList());
        when(dbFacade.getVdsDao()).thenReturn(hostDao);
        validator = new HostValidator(dbFacade, host);

        assertThat(validator.hostNameNotUsed(), isValid());
    }

    @Test
    public void hostNameIsUsed() {
        when(hostDao.getAllForHostname(any(String.class))).thenReturn(Collections.singletonList(mock(VDS.class)));
        when(dbFacade.getVdsDao()).thenReturn(hostDao);
        validator = new HostValidator(dbFacade, host);

        assertThat(validator.hostNameNotUsed(), failsWith(VdcBllMessages.ACTION_TYPE_FAILED_VDS_WITH_SAME_HOST_EXIST));
    }

    @Test
    public void portIsValid() {
        when(host.getSshPort()).thenReturn(BusinessEntitiesDefinitions.NETWORK_MIN_LEGAL_PORT);
        assertThat(validator.portIsValid(), isValid());
    }

    @Test
    public void portIsInvalid() {
        assertThat(validator.portIsValid(), failsWith(VdcBllMessages.ACTION_TYPE_FAILED_VDS_WITH_INVALID_SSH_PORT));
    }

    @Test
    public void sshUserNameNotEmpty() {
        when(host.getSshUsername()).thenReturn(RandomUtils.instance().nextString(HOST_NAME_SIZE));
        assertThat(validator.sshUserNameNotEmpty(), isValid());
    }

    @Test
    public void sshUserNameIsEmpty() {
        assertThat(validator.sshUserNameNotEmpty(),
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_VDS_WITH_INVALID_SSH_USERNAME));
    }

    @Test
    public void hostAttachedToLocalStorageWithoutDataCenter() {
        when(storagePoolDao.getForVdsGroup(any(Guid.class))).thenReturn(null);
        when(dbFacade.getStoragePoolDao()).thenReturn(storagePoolDao);
        validator = new HostValidator(dbFacade, host);

        assertThat(validator.validateSingleHostAttachedToLocalStorage(), isValid());
    }

    @Test
    public void validateSingleHostAttachedToLocalStorage() {
        StoragePool dataCenter = mock(StoragePool.class);
        when(dataCenter.isLocal()).thenReturn(true);
        when(storagePoolDao.getForVdsGroup(any(Guid.class))).thenReturn(dataCenter);
        when(dbFacade.getStoragePoolDao()).thenReturn(storagePoolDao);
        when(hostStaticDao.getAllForVdsGroup(any(Guid.class))).thenReturn(Collections.<VdsStatic> emptyList());
        when(dbFacade.getVdsStaticDao()).thenReturn(hostStaticDao);
        validator = new HostValidator(dbFacade, host);

        assertThat(validator.validateSingleHostAttachedToLocalStorage(), isValid());
    }

    @Test
    public void validateSingleHostAttachedToFewStorages() {
        StoragePool dataCenter = mock(StoragePool.class);
        when(dataCenter.isLocal()).thenReturn(true);
        when(storagePoolDao.getForVdsGroup(any(Guid.class))).thenReturn(dataCenter);
        when(dbFacade.getStoragePoolDao()).thenReturn(storagePoolDao);
        when(hostStaticDao.getAllForVdsGroup(any(Guid.class)))
                .thenReturn(Collections.<VdsStatic> singletonList(mock(VdsStatic.class)));
        when(dbFacade.getVdsStaticDao()).thenReturn(hostStaticDao);
        validator = new HostValidator(dbFacade, host);

        assertThat(validator.validateSingleHostAttachedToLocalStorage(),
                failsWith(VdcBllMessages.VDS_CANNOT_ADD_MORE_THEN_ONE_HOST_TO_LOCAL_STORAGE));
    }

    @Test
    public void securityKeysExists() {
        validator = spy(new HostValidator(dbFacade, host));
        doReturn(true).when(validator).haveSecurityKey();

        assertThat(validator.securityKeysExists(), isValid());
    }

    @Test
    public void securityKeysDoesNotExist() {
        validator = spy(new HostValidator(dbFacade, host));
        doReturn(false).when(validator).haveSecurityKey();

        assertThat(validator.securityKeysExists(),
                failsWith(VdcBllMessages.VDS_TRY_CREATE_SECURE_CERTIFICATE_NOT_FOUND));
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
                failsWith(VdcBllMessages.VDS_CANNOT_INSTALL_EMPTY_PASSWORD));
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
}
