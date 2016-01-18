package org.ovirt.engine.core.bll.profiles;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CpuProfileParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.profiles.ProfilesDao;

@RunWith(MockitoJUnitRunner.class)
public class AddCpuProfileCommandTest extends BaseCommandTest{
    private static final Guid CLUSTER_ID = Guid.newGuid();
    private static final String PROFILE_NAME = "profile name";
    private static final String SESSION_ID = "S3SS10N1D";
    private static final String CORRELATION_ID = "C0RR3LAT10N1D";

    @Mock
    private ProfilesDao<CpuProfile> cpuProfilesDao;

    private static AddCpuProfileCommand addCpuProfileCommand;
    private static CommandContext commandContext;

    @Mock
    private static BackendInternal backend;
    @Mock
    private static CpuProfileParameters parameters;

    @Before
    public void setUp() {
        createParameters();
        createCommandContext();
        createCommand();
        mockBackend();
    }

    private void createParameters() {
        CpuProfile cpuProfile = CpuProfileHelper.createCpuProfile(CLUSTER_ID, PROFILE_NAME);

        parameters = new CpuProfileParameters(cpuProfile);
        parameters.setCorrelationId(CORRELATION_ID);
        parameters.setAddPermissions(true);
        parameters.setSessionId(SESSION_ID);
    }

    private void createCommandContext() {
        commandContext = CommandContext.createContext(parameters.getSessionId());
    }

    private void createCommand() {
        AddCpuProfileCommand addCpuProfileCommandInstance = new AddCpuProfileCommand(parameters, commandContext) {
            @Override
            protected BackendInternal getBackend() {
                return backend;
            }
        };

        addCpuProfileCommand = spy(addCpuProfileCommandInstance);
        doReturn(cpuProfilesDao).when(addCpuProfileCommand).getProfileDao();
        doReturn(commandContext).when(addCpuProfileCommand).getContext();
    }

    private void mockBackend() {
        VdcReturnValueBase addCpuProfileReturnValue = mock(VdcReturnValueBase.class);
        when(addCpuProfileReturnValue.getSucceeded()).thenReturn(true);

        when(backend.runAction(any(VdcActionType.class), any(CpuProfileParameters.class))).thenReturn(addCpuProfileReturnValue);
    }

    @Test
    public void executeCommandTest() {
        addCpuProfileCommand.executeCommand();

        verify(addCpuProfileCommand).addPermissions();
        Assert.assertTrue(addCpuProfileCommand.getReturnValue().getSucceeded());
    }

    @Test
    public void getPermissionCheckSubjectsTest() {
        List<PermissionSubject> permissions = addCpuProfileCommand.getPermissionCheckSubjects();

        Assert.assertEquals(permissions.size(), 1);
        PermissionSubject permissionSubject = permissions.get(0);

        Assert.assertEquals(CLUSTER_ID, permissionSubject.getObjectId());
        Assert.assertEquals(VdcObjectType.Cluster, permissionSubject.getObjectType());
    }

    @Test
    public void getAuditLogTypeValueTest() {
        VdcReturnValueBase returnValueBase = new VdcReturnValueBase();

        returnValueBase.setSucceeded(true);
        addCpuProfileCommand.setReturnValue(returnValueBase);
        Assert.assertEquals(AuditLogType.USER_ADDED_CPU_PROFILE, addCpuProfileCommand.getAuditLogTypeValue());

        returnValueBase.setSucceeded(false);
        addCpuProfileCommand.setReturnValue(returnValueBase);
        Assert.assertEquals(AuditLogType.USER_FAILED_TO_ADD_CPU_PROFILE, addCpuProfileCommand.getAuditLogTypeValue());
    }
}
