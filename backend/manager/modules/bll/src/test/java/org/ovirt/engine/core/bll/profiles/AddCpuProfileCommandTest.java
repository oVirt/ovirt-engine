package org.ovirt.engine.core.bll.profiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.CpuProfileParameters;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;

public class AddCpuProfileCommandTest extends BaseCommandTest{
    private static final Guid CLUSTER_ID = Guid.newGuid();
    private static final String PROFILE_NAME = "profile name";
    private static final String SESSION_ID = "S3SS10N1D";
    private static final String CORRELATION_ID = "C0RR3LAT10N1D";

    @Mock
    private CpuProfileDao cpuProfilesDao;

    @Mock
    private BackendInternal backend;

    private CpuProfileParameters parameters = createParameters();
    private CommandContext commandContext = CommandContext.createContext(parameters.getSessionId());

    @InjectMocks
    @Spy
    private AddCpuProfileCommand addCpuProfileCommand = new AddCpuProfileCommand(parameters, commandContext);

    private static CpuProfileParameters createParameters() {
        CpuProfile cpuProfile = CpuProfileHelper.createCpuProfile(CLUSTER_ID, PROFILE_NAME);

        CpuProfileParameters parameters = new CpuProfileParameters(cpuProfile);
        parameters.setCorrelationId(CORRELATION_ID);
        parameters.setAddPermissions(true);
        parameters.setSessionId(SESSION_ID);
        return parameters;
    }

    @Test
    public void executeCommandTest() {
        addCpuProfileCommand.executeCommand();

        verify(addCpuProfileCommand).addPermissions();
        assertTrue(addCpuProfileCommand.getReturnValue().getSucceeded());
    }

    @Test
    public void getPermissionCheckSubjectsTest() {
        List<PermissionSubject> permissions = addCpuProfileCommand.getPermissionCheckSubjects();

        assertEquals(1, permissions.size());
        PermissionSubject permissionSubject = permissions.get(0);

        assertEquals(CLUSTER_ID, permissionSubject.getObjectId());
        assertEquals(VdcObjectType.Cluster, permissionSubject.getObjectType());
    }

    @Test
    public void getAuditLogTypeValueTest() {
        ActionReturnValue returnValueBase = new ActionReturnValue();

        returnValueBase.setSucceeded(true);
        addCpuProfileCommand.setReturnValue(returnValueBase);
        assertEquals(AuditLogType.USER_ADDED_CPU_PROFILE, addCpuProfileCommand.getAuditLogTypeValue());

        returnValueBase.setSucceeded(false);
        addCpuProfileCommand.setReturnValue(returnValueBase);
        assertEquals(AuditLogType.USER_FAILED_TO_ADD_CPU_PROFILE, addCpuProfileCommand.getAuditLogTypeValue());
    }
}
