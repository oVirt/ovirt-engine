package org.ovirt.engine.core.bll;

import org.junit.Test;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.businessentities.PermissionGrantMode;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class AddPermissionCommandTest extends BaseMockitoTest {

    @Test
    public void failAddingAutomaticPermissionByUser() {
        PermissionsOperationsParametes param = new PermissionsOperationsParametes();
        permissions automaticPermission = new permissions();
        automaticPermission.setGrantMode(PermissionGrantMode.Automatic);
        param.setPermission(automaticPermission);
        AddPermissionCommand<PermissionsOperationsParametes> cmd =
                new AddPermissionCommand<PermissionsOperationsParametes>(param);
        cmd.setInternalExecution(false);
        assertFalse("Illegal permission grant mode", cmd.canDoAction());
        checkSucceeded(cmd, false);
        checkMessages(cmd, VdcBllMessages.PERMISSION_ADD_FAILED_ILLEGAL_GRANT_MODE);
    }

}
