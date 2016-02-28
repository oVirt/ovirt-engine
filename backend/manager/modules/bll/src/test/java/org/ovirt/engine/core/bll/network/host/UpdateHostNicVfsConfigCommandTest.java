package org.ovirt.engine.core.bll.network.host;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.VfsConfigValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.UpdateHostNicVfsConfigParameters;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.HostNicVfsConfigDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;

public class UpdateHostNicVfsConfigCommandTest extends BaseCommandTest {

    private UpdateHostNicVfsConfigParameters param;
    private UpdateHostNicVfsConfigCommand command;

    private static final Guid NIC_ID = Guid.newGuid();
    private static final int NUM_OF_VFS = 5;

    @Mock
    InterfaceDao interfaceDao;

    @Mock
    HostNicVfsConfigDao vfsConfigDao;

    @Mock
    HostNicVfsConfig oldVfsConfig;

    @Mock
    VfsConfigValidator validator;

    @Before
    public void setup() {
        createCommand();
        setAllValidationsValid();
    }

    public void createCommand() {
        param = new UpdateHostNicVfsConfigParameters(NIC_ID, NUM_OF_VFS, false);

        command = spy(new UpdateHostNicVfsConfigCommand(param, null));
        doReturn(validator).when(command).getVfsConfigValidator();
        doReturn(vfsConfigDao).when(command).getVfsConfigDao();
        doReturn(interfaceDao).when(command).getInterfaceDao();
    }

    @Test
    public void nicNotExist() {
        nicExists(false);
        assertValidateFailure(EngineMessage.HOST_NETWORK_INTERFACE_NOT_EXIST.toString());
    }

    @Test
    public void nicNotSriovEnabled() {
        nicSriovEnabled(false);
        assertValidateFailure(EngineMessage.ACTION_TYPE_FAILED_NIC_IS_NOT_SRIOV_ENABLED.toString());
    }

    @Test
    public void notAllVfsAreFree() {
        allVfsAreFree(false);
        assertValidateFailure(EngineMessage.ACTION_TYPE_FAILED_NUM_OF_VFS_CANNOT_BE_CHANGED.toString());
    }

    @Test
    public void numOfVfsIsNotInRange() {
        numOfVfsInValidRange(false);
        assertValidateFailure(EngineMessage.ACTION_TYPE_FAILED_NUM_OF_VFS_NOT_IN_VALID_RANGE.toString());
    }

    @Test
    public void validateSuccessNumOfVfsNotChanged() {
        doReturn(false).when(command).wasNumOfVfsChanged();
        allVfsAreFree(false);
        numOfVfsInValidRange(false);
        assertValidateSuccess();
    }

    @Test
    public void validateSuccessNumOfVfsChanged() {
        assertValidateSuccess();
    }

    @Test
    public void executionFailure() {
        doReturn(null).when(command).getVfsConfig();
        doReturn(null).when(command).getNic();
        assertExecuteActionFailure();
    }

    private void setAllValidationsValid() {
        doReturn(true).when(command).wasNumOfVfsChanged();
        nicExists(true);
        nicSriovEnabled(true);
        allVfsAreFree(true);
        numOfVfsInValidRange(true);
    }

    private void nicExists(boolean isValid) {
        when(validator.nicExists()).thenReturn(isValid ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.HOST_NETWORK_INTERFACE_NOT_EXIST));
    }

    private void nicSriovEnabled(boolean isValid) {
        when(validator.nicSriovEnabled()).thenReturn(isValid ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_NIC_IS_NOT_SRIOV_ENABLED));
    }

    private void allVfsAreFree(boolean isValid) {
        when(validator.allVfsAreFree(any(NetworkDeviceHelper.class))).thenReturn(isValid ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_NUM_OF_VFS_CANNOT_BE_CHANGED));
    }

    private void numOfVfsInValidRange(boolean isValid) {
        when(validator.numOfVfsInValidRange(param.getNumOfVfs())).thenReturn(isValid ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_NUM_OF_VFS_NOT_IN_VALID_RANGE));
    }

    private void assertValidateFailure(final String messageToVerify) {
        assertFalse(command.validate());
        assertTrue(command.getReturnValue().getValidationMessages().contains(messageToVerify));
    }

    private void assertValidateSuccess() {
        assertTrue(command.validate());
        assertTrue(command.getReturnValue().getValidationMessages().isEmpty());
    }

    private void assertExecuteActionFailure() {
        try {
            command.executeCommand();
        } catch (Exception expected) {
            // An exception is expected here
        }

        assertFalse(command.getReturnValue().getSucceeded());
        assertEquals(AuditLogType.UPDATE_HOST_NIC_VFS_CONFIG_FAILED, command.getAuditLogTypeValue());
    }
}
