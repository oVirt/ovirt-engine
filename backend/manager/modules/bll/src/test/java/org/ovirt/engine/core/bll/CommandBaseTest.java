package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.test.InMemoryLoggingHandler;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSGenericException;

/** A test case for {@link CommandBase} */
@ExtendWith(MockConfigExtension.class)
public class CommandBaseTest extends BaseCommandTest {
    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.UserSessionTimeOutInterval, 30));
    }

    protected String session = "someSession";

    private static final String ERROR_MESSAGE = "Custom error message";

    @Mock
    private AuditLogDirector director;

    @InjectMocks
    private CommandBase<ActionParametersBase> command = mock(
            CommandBase.class,
            withSettings().defaultAnswer(Answers.CALLS_REAL_METHODS)
                    .extraInterfaces(RenamedEntityInfoProvider.class)
                    .useConstructor(new ActionParametersBase(), CommandContext.createContext(session)));

    @BeforeEach
    public void setupEnvironment() {
        CorrelationIdTracker.clean();
        DbUser user = mock(DbUser.class);

        when(engineSessionDao.remove(anyLong())).thenReturn(1);
        sessionDataContainer.setUser(session, user);
    }

    @AfterEach
    public void clearEnvironment() {
        CorrelationIdTracker.clean();
        sessionDataContainer.removeSessionOnLogout(session);
    }

    /** Testing the constructor, which adds the user id to the thread local container */
    @Test
    public void testConstructor() {
        DbUser user = mock(DbUser.class);
        when(user.getId()).thenReturn(Guid.EVERYONE);

        // Mock the session
        sessionDataContainer.setUser(session, user);

        // Create a command
        command.postConstruct();

        // Check the session
        assertEquals(user.getId(), command.getUserId(), "wrong user id on command");
    }

    @Test
    public void logRenamedEntityNotRename() {
        command.logRenamedEntity();
    }

    @Test
    public void logRenamedEntity() {
        RenamedEntityInfoProvider infoProvider = (RenamedEntityInfoProvider) command;
        command.logRenamedEntity();
        when(infoProvider.getEntityOldName()).thenReturn("foo");
        when(infoProvider.getEntityNewName()).thenReturn("bar");
        when(command.getCurrentUser()).thenReturn(mock(DbUser.class));
        command.logRenamedEntity();
        when(infoProvider.getEntityNewName()).thenReturn("bar");
        command.logRenamedEntity();
        when(infoProvider.getEntityOldName()).thenReturn("foo");
        command.logRenamedEntity();
    }

    @Test
    public void testExtractVariableDeclarationsForStaticMsgs() {
        List<String> msgs = Arrays.asList(
                "ACTION_TYPE_FAILED_TEMPLATE_IS_USED_FOR_CREATE_VM",
                "IRS_FAILED_RETRIEVING_SNAPSHOT_INFO");

        assertTrue(CollectionUtils.isEqualCollection(msgs, command.extractVariableDeclarations(msgs)),
                "extractVariableDeclarations didn't return the same static messages");
    }

    @Test
    public void testExtractVariableDeclarationsForDynamicMsgs() {
        String msg1_1 = "ACTION_TYPE_FAILED_TEMPLATE_IS_USED_FOR_CREATE_VM";
        String msg1_2 = "$VmName Vm1";
        String msg2   = "IRS_FAILED_CREATING_SNAPSHOT";
        String msg3_1 = "ACTION_TYPE_FAILED_VM_SNAPSHOT_HAS_NO_CONFIGURATION";
        String msg3_2 = "$VmName Vm2";
        String msg3_3 = "$SnapshotName Snapshot";
        List<String> appendedMsgs = Arrays.asList(
                new StringBuilder().append(msg1_1).append(msg1_2).toString(),
                msg2,
                new StringBuilder().append(msg3_1).append(msg3_2).append(msg3_3).toString());
        List<String> extractedMsgs = Arrays.asList(msg1_1, msg1_2, msg2, msg3_1, msg3_2, msg3_3);

        assertTrue(CollectionUtils.isEqualCollection(extractedMsgs, command.extractVariableDeclarations(appendedMsgs)),
                "extractVariableDeclarations didn't extract the variables as expected");
    }

    @ParameterizedTest
    @MethodSource("sourceForTestExceptionsDuringAuditLog")
    public void testExceptionsDuringAuditLog(RuntimeException ex, boolean result) {
        doThrow(ex).when(director).log(any(AuditLogable.class), any(AuditLogType.class));

        InMemoryLoggingHandler handler = new InMemoryLoggingHandler();
        Logger logger = Logger.getLogger("org.ovirt.engine.core.bll");
        logger.addHandler(handler);

        command.log();

        assertFalse(handler.getLogRecords().isEmpty());
        assertNotNull(handler.getLogRecords().get(0).getMessage());
        assertEquals(result, handler.getLogRecords().get(0).getMessage().contains(ERROR_MESSAGE));
    }

    public static Stream<Arguments> sourceForTestExceptionsDuringAuditLog() {
        return Stream.of(
                // top level exception with non-null error message
                Arguments.of(
                        new RuntimeException(ERROR_MESSAGE),
                        true),

                // top level exception with null error message
                Arguments.of(
                        new RuntimeException((String) null),
                        false),

                // encapsulated exception
                Arguments.of(
                        new RuntimeException(new VDSGenericException(ERROR_MESSAGE)),
                        true),

                // encapsulated exception with top level null error message
                Arguments.of(
                        new RuntimeException(null, new VDSGenericException(ERROR_MESSAGE)),
                        true));
    }
}
