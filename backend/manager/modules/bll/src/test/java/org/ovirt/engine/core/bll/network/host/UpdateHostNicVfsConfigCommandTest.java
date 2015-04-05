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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.VfsConfigValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.UpdateHostNicVfsConfigParameters;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.HostNicVfsConfigDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;

@RunWith(MockitoJUnitRunner.class)
public class UpdateHostNicVfsConfigCommandTest {

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

        command = spy(new UpdateHostNicVfsConfigCommand(param));
        doReturn(validator).when(command).getVfsConfigValidator();
        doReturn(vfsConfigDao).when(command).getVfsConfigDao();
        doReturn(interfaceDao).when(command).getInterfaceDao();
    }

    @Test
    public void nicNotExist() {
        nicExists(false);
        assertCanDoActionFailure(VdcBllMessages.HOST_NETWORK_INTERFACE_NOT_EXIST.toString());
    }

    @Test
    public void sriovFeatureIsNotSupported() {
        sriovFeatureSupported(false);
        assertCanDoActionFailure(VdcBllMessages.ACTION_TYPE_FAILED_SRIOV_FEATURE_NOT_SUPPORTED.toString());
    }

    @Test
    public void nicNotSriovEnabled() {
        nicSriovEnabled(false);
        assertCanDoActionFailure(VdcBllMessages.ACTION_TYPE_FAILED_NIC_IS_NOT_SRIOV_ENABLED.toString());
    }

    @Test
    public void notAllVfsAreFree() {
        allVfsAreFree(false);
        assertCanDoActionFailure(VdcBllMessages.ACTION_TYPE_FAILED_NUM_OF_VFS_CANNOT_BE_CHANGED.toString());
    }

    @Test
    public void numOfVfsIsNotInRange() {
        numOfVfsInValidRange(false);
        assertCanDoActionFailure(VdcBllMessages.ACTION_TYPE_FAILED_NUM_OF_VFS_NOT_IN_VALID_RANGE.toString());
    }

    @Test
    public void canDoActionSuccessNumOfVfsNotChanged() {
        doReturn(false).when(command).wasNumOfVfsChanged();
        allVfsAreFree(false);
        numOfVfsInValidRange(false);
        assertCanDoActionSuccess();
    }

    @Test
    public void canDoActionSuccessNumOfVfsChanged() {
        assertCanDoActionSuccess();
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
        sriovFeatureSupported(true);
        nicSriovEnabled(true);
        allVfsAreFree(true);
        numOfVfsInValidRange(true);
    }

    private void nicExists(boolean isValid) {
        when(validator.nicExists()).thenReturn(isValid ? ValidationResult.VALID
                : new ValidationResult(VdcBllMessages.HOST_NETWORK_INTERFACE_NOT_EXIST));
    }

    private void sriovFeatureSupported(boolean isValid) {
        when(validator.sriovFeatureSupported()).thenReturn(isValid ? ValidationResult.VALID
                : new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_SRIOV_FEATURE_NOT_SUPPORTED));
    }

    private void nicSriovEnabled(boolean isValid) {
        when(validator.nicSriovEnabled()).thenReturn(isValid ? ValidationResult.VALID
                : new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_NIC_IS_NOT_SRIOV_ENABLED));
    }

    private void allVfsAreFree(boolean isValid) {
        when(validator.allVfsAreFree(any(HostNicVfsConfigHelper.class))).thenReturn(isValid ? ValidationResult.VALID
                : new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_NUM_OF_VFS_CANNOT_BE_CHANGED));
    }

    private void numOfVfsInValidRange(boolean isValid) {
        when(validator.numOfVfsInValidRange(param.getNumOfVfs())).thenReturn(isValid ? ValidationResult.VALID
                : new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_NUM_OF_VFS_NOT_IN_VALID_RANGE));
    }

    private void assertCanDoActionFailure(final String messageToVerify) {
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue().getCanDoActionMessages().contains(messageToVerify));
    }

    private void assertCanDoActionSuccess() {
        assertTrue(command.canDoAction());
        assertTrue(command.getReturnValue().getCanDoActionMessages().isEmpty());
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
