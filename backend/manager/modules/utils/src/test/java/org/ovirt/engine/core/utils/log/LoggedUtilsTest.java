package org.ovirt.engine.core.utils.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.slf4j.Logger;

/**
 * Tests for the {@link LoggedUtils} class.
 */
public class LoggedUtilsTest {

    /* --- Types used for testing purposes --- */

    /**
     * Interface used for DRY testing of {@link LoggedUtils#log}.
     */
    private static interface LogSetup {
        public void setup(Logger mock, String message, Object... args);

        public void verifyCall(Logger mock, String message, Object... args);
    }

    @Logged(executionLevel = LogLevel.OFF, errorLevel = LogLevel.OFF)
    public class LoggedClass {
        // Intentionally empty - a stub class for testing
    }

    public class LoggedSubclass extends LoggedClass {
        // Intentionally empty - a stub class for testing
    }

    @Logged(executionLevel = LogLevel.DEBUG, errorLevel = LogLevel.WARN,
            parametersLevel = LogLevel.FATAL, returnLevel = LogLevel.FATAL)
    public class LoggedOverridingSubclass extends LoggedClass {
        // Intentionally empty - a stub class for testing
    }

    @Logged(parametersLevel = LogLevel.DEBUG)
    public class LoggedOverridingSubclassNoParameters extends LoggedClass {
        // Intentionally empty - a stub class for testing
    }

    @Logged(returnLevel = LogLevel.DEBUG)
    public class LoggedOverridingSubclassNoReturn extends LoggedClass {
        // Intentionally empty - a stub class for testing
    }

    /* --- Tests for the method "getObjectId" --- */

    @Test
    public void testGetObjectIdFromNull() {
        assertEquals("0", LoggedUtils.getObjectId(null));
    }

    @Test
    public void testGetObjectIdUniqueForDifferentObjects() {
        assertTrue(!LoggedUtils.getObjectId(new Object()).equals(LoggedUtils.getObjectId(new Object())));
    }

    @Test
    public void testGetObjectIdConsistentForSameObject() {
        Object obj = new Object();
        assertEquals(LoggedUtils.getObjectId(obj), LoggedUtils.getObjectId(obj));
    }

    /* --- Tests for the method "getAnnotation" --- */

    @Test
    public void testGetAnnotationFromNull() {
        assertNull(LoggedUtils.getAnnotation(null));
    }

    @Test
    public void testGetAnnotationFromClass() {
        Logged logged = LoggedUtils.getAnnotation(new LoggedClass());

        assertEquals(LogLevel.OFF, logged.executionLevel());
        assertEquals(LogLevel.OFF, logged.errorLevel());
        assertEquals(LogLevel.INFO, logged.parametersLevel());
        assertEquals(LogLevel.INFO, logged.returnLevel());
    }

    @Test
    public void testGetAnnotationFromSubclass() {
        assertSame(LoggedUtils.getAnnotation(new LoggedClass()), LoggedUtils.getAnnotation(new LoggedSubclass()));
    }

    @Test
    public void testGetAnnotationFromOverridingSubclass() {
        Logged logged = LoggedUtils.getAnnotation(new LoggedOverridingSubclass());

        assertEquals(LogLevel.DEBUG, logged.executionLevel());
        assertEquals(LogLevel.WARN, logged.errorLevel());
        assertEquals(LogLevel.FATAL, logged.parametersLevel());
        assertEquals(LogLevel.FATAL, logged.returnLevel());
    }

    /* --- Tests for the method "determineMessage" --- */

    @Test
    public void testDetermineMessageReturnsObjectForParameterExpansion() {
        Object obj = new Object();
        Logger log = mock(Logger.class);
        when(log.isDebugEnabled()).thenReturn(true);

        assertSame(obj,
                LoggedUtils.determineMessage(log, LoggedOverridingSubclass.class.getAnnotation(Logged.class), obj));
    }

    @Test
    public void testDetermineMessageReturnsClassNameForNoParameterExpansion() {
        Logger log = mock(Logger.class);

        assertEquals(
                Object.class.getName(),
                LoggedUtils.determineMessage(log,
                        LoggedOverridingSubclassNoParameters.class.getAnnotation(Logged.class), new Object()),
                "LoggedUtils.determineMessage shouldn't return parameter expansion for a disabled log level.");
        assertEquals(
                Object.class.getName(),
                LoggedUtils.determineMessage(log, LoggedClass.class.getAnnotation(Logged.class), new Object()),
                "LoggedUtils.determineMessage shouldn't return parameter expansion when diabled completely.");
    }

    /* --- Tests for the method "log" --- */

    @Test
    public void testLogOffDoesntLog() {
        Logger log = mock(Logger.class);

        LoggedUtils.log(log, LogLevel.OFF, "{}", new Object());
        verifyZeroInteractions(log);
    }

    @Test
    public void testLogTrace() {
        helpTestLog(LogLevel.TRACE, new LogSetup() {
            @Override
            public void setup(Logger mock, String message, Object... args) {
                when(mock.isTraceEnabled()).thenReturn(true);
            }

            @Override
            public void verifyCall(Logger mock, String message, Object... args) {
                verify(mock).trace(message, args);
            }
        });
    }

    @Test
    public void testLogDebug() {
        helpTestLog(LogLevel.DEBUG, new LogSetup() {
            @Override
            public void setup(Logger mock, String message, Object... args) {
                when(mock.isDebugEnabled()).thenReturn(true);
            }

            @Override
            public void verifyCall(Logger mock, String message, Object... args) {
                verify(mock).debug(message, args);
            }
        });
    }

    @Test
    public void testLogInfo() {
        helpTestLog(LogLevel.INFO, new LogSetup() {
            @Override
            public void setup(Logger mock, String message, Object... args) {
                when(mock.isInfoEnabled()).thenReturn(true);
            }

            @Override
            public void verifyCall(Logger mock, String message, Object... args) {
                verify(mock).info(message, args);
            }
        });
    }

    @Test
    public void testLogWarn() {
        helpTestLog(LogLevel.WARN, new LogSetup() {
            @Override
            public void setup(Logger mock, String message, Object... args) {
                when(mock.isWarnEnabled()).thenReturn(true);
            }

            @Override
            public void verifyCall(Logger mock, String message, Object... args) {
                verify(mock).warn(message, args);
            }
        });
    }

    @Test
    public void testLogError() {
        helpTestLog(LogLevel.ERROR, new LogSetup() {
            @Override
            public void setup(Logger mock, String message, Object... args) {
                when(mock.isErrorEnabled()).thenReturn(true);
            }

            @Override
            public void verifyCall(Logger mock, String message, Object... args) {
                verify(mock).error(message, args);
            }
        });
    }

    @Test
    public void testLogFatal() {
        helpTestLog(LogLevel.FATAL, new LogSetup() {
            @Override
            public void setup(Logger mock, String message, Object... args) {
                when(mock.isErrorEnabled()).thenReturn(true);
            }

            @Override
            public void verifyCall(Logger mock, String message, Object... args) {
                verify(mock).error(message, args);
            }
        });
    }

    /* --- Tests for the method "logEntry" --- */

    @Test
    public void testLogEntryDoesntLogWhenNoAnnotation() {
        Logger log = mock(Logger.class);
        LoggedUtils.logEntry(log, "", new Object());
        verifyZeroInteractions(log);
    }

    @Test
    public void testLogEntryDoesntLogWhenLogLevelInactive() {
        Logger log = mock(Logger.class);
        LoggedUtils.logEntry(log, "", new LoggedOverridingSubclass());
        verifyNoLogging(log);
    }

    @Test
    public void testLogEntryLogsWhenLogLevelActive() {
        String id = "";
        Logger log = mock(Logger.class);
        when(log.isDebugEnabled()).thenReturn(true);

        LoggedUtils.logEntry(log, id, new LoggedOverridingSubclass());
        verify(log).debug(eq(LoggedUtils.ENTRY_LOG), new Object[] {any(), eq(id)});
    }

    /* --- Tests for the method "logReturn" --- */

    @Test
    public void testLogReturnDoesntLogWhenNoAnnotation() {
        Logger log = mock(Logger.class);
        LoggedUtils.logReturn(log, "", new Object(), new Object());
        verifyZeroInteractions(log);
    }

    @Test
    public void testLogReturnDoesntLogWhenLogLevelInactive() {
        Logger log = mock(Logger.class);
        LoggedUtils.logReturn(log, "", new LoggedOverridingSubclass(), new Object());
        verifyNoLogging(log);
    }

    @Test
    public void testLogReturnLogsWhenLogLevelActiveAndNoExpandReturn() {
        String id = "";
        Logger log = mock(Logger.class);
        when(log.isInfoEnabled()).thenReturn(true);

        LoggedUtils.logReturn(log, id, new LoggedOverridingSubclassNoReturn(), new Object());
        verify(log).info(eq(LoggedUtils.EXIT_LOG_VOID), new Object[] {any(), eq(id)});
    }

    @Test
    public void testLogReturnLogsWhenLogLevelActiveAndExpandReturn() {
        String id = "";
        Logger log = mock(Logger.class);
        when(log.isDebugEnabled()).thenReturn(true);

        LoggedUtils.logReturn(log, id, new LoggedOverridingSubclass(), new Object());
        verify(log).debug(eq(LoggedUtils.EXIT_LOG_RETURN_VALUE), any(), any(), eq(id));
    }

    @Test
    public void testLogReturnLogsWhenLogLevelActiveAndExpandReturnButNullReturn() {
        String id = "";
        Logger log = mock(Logger.class);
        when(log.isDebugEnabled()).thenReturn(true);

        LoggedUtils.logReturn(log, id, new LoggedOverridingSubclass(), null);
        verify(log).debug(eq(LoggedUtils.EXIT_LOG_VOID), new Object[] {any(), eq("")});
    }

    /* --- Tests for the method "logError" --- */

    @Test
    public void testLogErrorDoesntLogWhenNoAnnotation() {
        Logger log = mock(Logger.class);
        LoggedUtils.logError(log, "", new Object(), new Exception());
        verifyZeroInteractions(log);
    }

    @Test
    public void testLogErrorDoesntLogWhenLogLevelInactive() {
        Logger log = mock(Logger.class);
        LoggedUtils.logError(log, "", new LoggedOverridingSubclass(), new Exception());
        verifyNoLogging(log);
    }

    @Test
    public void testLogErrorLogsWhenLogLevelActive() {
        String id = "";
        Logger log = mock(Logger.class);

        // when the call to determine whether to log the parameters or not.
        when(log.isDebugEnabled()).thenReturn(true);
        when(log.isWarnEnabled()).thenReturn(true);

        Exception e = new Exception("Test");
        LoggedUtils.logError(log, id, new LoggedOverridingSubclass(), e);
        verify(log).warn(eq(LoggedUtils.ERROR_LOG), any(), any(), eq(id));
        verify(log).error(eq("Exception {}"), eq(ExceptionUtils.getRootCauseMessage(e)));
    }

    /* --- Helper methods --- */

    /**
     * Verifies that no logging was done on the given log mock.
     */
    private static void verifyNoLogging(Logger logMock) {
        verify(logMock, never()).trace(any(), (Object) any());
        verify(logMock, never()).debug(any(), (Object) any());
        verify(logMock, never()).info(any(), (Object) any());
        verify(logMock, never()).warn(any(), (Object) any());
        verify(logMock, never()).error(any(), (Object) any());
    }

    /**
     * Helper method to test the {@link LoggedUtils#log} method functionality.
     * @param logLevel Log level to test.
     * @param logSetup Setup the mocks using an anonymous inner class of this interface.
     */
    private static void helpTestLog(LogLevel logLevel, LogSetup logSetup) {
        Logger log = mock(Logger.class);
        String message = "{}";
        Object s = "arg1";
        logSetup.setup(log, message, s);
        LoggedUtils.log(log, logLevel, message, s);
        logSetup.verifyCall(log, message, s);
    }
}
