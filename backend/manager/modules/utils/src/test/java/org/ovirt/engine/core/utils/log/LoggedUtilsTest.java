package org.ovirt.engine.core.utils.log;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;

/**
 * Tests for the {@link LoggedUtils} class.
 */
public class LoggedUtilsTest {


    /* --- Types used for testing purposes --- */

    /**
     * Interface used for DRY testing of {@link LoggedUtils#log}.
     */
    private static interface LogSetup {
        public void setup(Log mock, String message, Object args);
    }

    @Logged(executionLevel = LogLevel.OFF, errorLevel = LogLevel.OFF)
    public class LoggedClass {}

    public class LoggedSubclass extends LoggedClass {}

    @Logged(executionLevel = LogLevel.DEBUG, errorLevel = LogLevel.WARN,
            parametersLevel = LogLevel.FATAL, returnLevel = LogLevel.FATAL)
    public class LoggedOverridingSubclass extends LoggedClass {}

    @Logged(parametersLevel = LogLevel.DEBUG)
    public class LoggedOverridingSubclassNoParameters extends LoggedClass {}

    @Logged(returnLevel = LogLevel.DEBUG)
    public class LoggedOverridingSubclassNoReturn extends LoggedClass {}


    /* --- Tests for the method "getObjectId" --- */

    @Test
    public void testGetObjectIdFromNull() throws Exception {
        assertEquals("0", LoggedUtils.getObjectId(null));
    }

    @Test
    public void testGetObjectIdUniqueForDifferentObjects() throws Exception {
        assertTrue(! LoggedUtils.getObjectId(new Object()).equals(LoggedUtils.getObjectId(new Object())) );
    }

    @Test
    public void testGetObjectIdConsistentForSameObject() throws Exception {
        Object obj = new Object();
        assertEquals(LoggedUtils.getObjectId(obj), LoggedUtils.getObjectId(obj));
    }


    /* --- Tests for the method "getAnnotation" --- */

    @Test
    public void testGetAnnotationFromNull() throws Exception {
        assertNull(LoggedUtils.getAnnotation(null));
    }

    @Test
    public void testGetAnnotationFromClass() throws Exception {
        Logged logged = LoggedUtils.getAnnotation(new LoggedClass());

        assertEquals(LogLevel.OFF, logged.executionLevel());
        assertEquals(LogLevel.OFF, logged.errorLevel());
        assertEquals(LogLevel.INFO, logged.parametersLevel());
        assertEquals(LogLevel.INFO, logged.returnLevel());
    }

    @Test
    public void testGetAnnotationFromSubclass() throws Exception {
        assertSame(LoggedUtils.getAnnotation(new LoggedClass()), LoggedUtils.getAnnotation(new LoggedSubclass()));
    }

    @Test
    public void testGetAnnotationFromOverridingSubclass() throws Exception {
        Logged logged = LoggedUtils.getAnnotation(new LoggedOverridingSubclass());

        assertEquals(LogLevel.DEBUG, logged.executionLevel());
        assertEquals(LogLevel.WARN, logged.errorLevel());
        assertEquals(LogLevel.FATAL, logged.parametersLevel());
        assertEquals(LogLevel.FATAL, logged.returnLevel());
    }


    /* --- Tests for the method "determineMessage" --- */

    @Test
    public void testDetermineMessageReturnsObjectForParameterExpansion() throws Exception {
        Object obj = new Object();
        Log log = createMock(Log.class);
        expect(log.isDebugEnabled()).andReturn(true);
        replay(log);

        assertSame(obj, LoggedUtils.determineMessage(log, LoggedOverridingSubclass.class.getAnnotation(Logged.class), obj));
    }

    @Test
    public void testDetermineMessageReturnsClassNameForNoParameterExpansion() throws Exception {
        Log log = createMock(Log.class);
        expect(log.isDebugEnabled()).andReturn(false).anyTimes();
        replay(log);

        assertEquals("LoggedUtils.determineMessage shouldn't return parameter expansion for a disabled log level.",
                Object.class.getName(),
                LoggedUtils.determineMessage(log,
                        LoggedOverridingSubclassNoParameters.class.getAnnotation(Logged.class), new Object()));
        assertEquals("LoggedUtils.determineMessage shouldn't return parameter expansion when diabled completely.",
                Object.class.getName(),
                LoggedUtils.determineMessage(log, LoggedClass.class.getAnnotation(Logged.class), new Object()));
    }


    /* --- Tests for the method "log" --- */

    @Test
    public void testLogOffDoesntLog() {
        Log log = createMock(Log.class);
        replay(log);

        LoggedUtils.log(log, LogLevel.OFF, "{0}", new Object());
        verify(log);
    }

    @Test
    public void testLogTrace() throws Exception {
        helpTestLog(LogLevel.TRACE, new LogSetup() {
            @Override
            public void setup(Log mock, String message, Object args) {
                expect(mock.isTraceEnabled()).andReturn(true);
                mock.traceFormat(message, args);
            }
        });
    }

    @Test
    public void testLogDebug() throws Exception {
        helpTestLog(LogLevel.DEBUG, new LogSetup() {
            @Override
            public void setup(Log mock, String message, Object args) {
                expect(mock.isDebugEnabled()).andReturn(true);
                mock.debugFormat(message, args);
            }
        });
    }

    @Test
    public void testLogInfo() throws Exception {
        helpTestLog(LogLevel.INFO, new LogSetup() {
            @Override
            public void setup(Log mock, String message, Object args) {
                expect(mock.isInfoEnabled()).andReturn(true);
                mock.infoFormat(message, args);
            }
        });
    }

    @Test
    public void testLogWarn() throws Exception {
        helpTestLog(LogLevel.WARN, new LogSetup() {
            @Override
            public void setup(Log mock, String message, Object args) {
                expect(mock.isWarnEnabled()).andReturn(true);
                mock.warnFormat(message, args);
            }
        });
    }

    @Test
    public void testLogError() throws Exception {
        helpTestLog(LogLevel.ERROR, new LogSetup() {
            @Override
            public void setup(Log mock, String message, Object args) {
                expect(mock.isErrorEnabled()).andReturn(true);
                mock.errorFormat(message, args);
            }
        });
    }

    @Test
    public void testLogFatal() throws Exception {
        helpTestLog(LogLevel.FATAL, new LogSetup() {
            @Override
            public void setup(Log mock, String message, Object args) {
                expect(mock.isFatalEnabled()).andReturn(true);
                mock.fatalFormat(message, args);
            }
        });
    }


    /* --- Tests for the method "logEntry" --- */

    @Test
    public void testLogEntryDoesntLogWhenNoAnnotation() throws Exception {
        Log log = createMock(Log.class);
        replay(log);
        LoggedUtils.logEntry(log, "", new Object());
    }

    @Test
    public void testLogEntryDoesntLogWhenLogLevelInactive() throws Exception {
        Log log = createMock(Log.class);
        expect(log.isDebugEnabled()).andReturn(false);
        replay(log);

        LoggedUtils.logEntry(log, "", new LoggedOverridingSubclass());
    }

    @Test
    public void testLogEntryLogsWhenLogLevelActive() throws Exception {
        String id = "";
        Log log = createMock(Log.class);
        expect(log.isDebugEnabled()).andReturn(true).anyTimes();
        log.debugFormat(eq(LoggedUtils.ENTRY_LOG), anyObject(), eq(id));
        replay(log);

        LoggedUtils.logEntry(log, id, new LoggedOverridingSubclass());
    }


    /* --- Tests for the method "logReturn" --- */

    @Test
    public void testLogReturnDoesntLogWhenNoAnnotation() throws Exception {
        Log log = createMock(Log.class);
        replay(log);
        LoggedUtils.logReturn(log, "", new Object(), new Object());
    }

    @Test
    public void testLogReturnDoesntLogWhenLogLevelInactive() throws Exception {
        Log log = createMock(Log.class);
        expect(log.isDebugEnabled()).andReturn(false);
        replay(log);

        LoggedUtils.logReturn(log, "", new LoggedOverridingSubclass(), new Object());
    }

    @Test
    public void testLogReturnLogsWhenLogLevelActiveAndNoExpandReturn() throws Exception {
        String id = "";
        Log log = createMock(Log.class);
        expect(log.isInfoEnabled()).andReturn(true).anyTimes();
        expect(log.isDebugEnabled()).andReturn(false);
        log.infoFormat(eq(LoggedUtils.EXIT_LOG_VOID), anyObject(), eq(id));
        replay(log);

        LoggedUtils.logReturn(log, id, new LoggedOverridingSubclassNoReturn(), new Object());
    }

    @Test
    public void testLogReturnLogsWhenLogLevelActiveAndExpandReturn() throws Exception {
        String id = "";
        Log log = createMock(Log.class);
        expect(log.isDebugEnabled()).andReturn(true).anyTimes();
        log.debugFormat(eq(LoggedUtils.EXIT_LOG_RETURN_VALUE), anyObject(), anyObject(), eq(id));
        replay(log);

        LoggedUtils.logReturn(log, id, new LoggedOverridingSubclass(), new Object());
    }

    @Test
    public void testLogReturnLogsWhenLogLevelActiveAndExpandReturnButNullReturn() throws Exception {
        String id = "";
        Log log = createMock(Log.class);
        expect(log.isDebugEnabled()).andReturn(true).anyTimes();
        log.debugFormat(eq(LoggedUtils.EXIT_LOG_VOID), anyObject(), eq(id));
        replay(log);

        LoggedUtils.logReturn(log, id, new LoggedOverridingSubclass(), null);
    }


    /* --- Tests for the method "logError" --- */

    @Test
    public void testLogErrorDoesntLogWhenNoAnnotation() throws Exception {
        Log log = createMock(Log.class);
        replay(log);
        LoggedUtils.logError(log, "", new Object(), new Exception());
    }

    @Test
    public void testLogErrorDoesntLogWhenLogLevelInactive() throws Exception {
        Log log = createMock(Log.class);
        expect(log.isWarnEnabled()).andReturn(false);
        replay(log);

        LoggedUtils.logError(log, "", new LoggedOverridingSubclass(), new Exception());
    }

    @Test
    public void testLogErrorLogsWhenLogLevelActive() throws Exception {
        String id = "";
        Log log = createMock(Log.class);

        // Expect the call to determine whether to log the parameters or not.
        expect(log.isDebugEnabled()).andReturn(true);
        expect(log.isWarnEnabled()).andReturn(true).anyTimes();
        log.warnFormat(eq(LoggedUtils.ERROR_LOG), anyObject(), anyObject(), eq(id), anyObject());
        replay(log);

        LoggedUtils.logError(log, id, new LoggedOverridingSubclass(), new Exception());
    }


    /* --- Helper methods --- */

    /**
     * Helper method to test the {@link LoggedUtils#log} method functionality.
     * @param logLevel Log level to test.
     * @param logSetup Setup the mocks using an anonymous inner class of this interface.
     */
    private void helpTestLog(LogLevel logLevel, LogSetup logSetup) {
        Log log = createMock(Log.class);
        String message = "{0}";
        Object args = new Object();
        logSetup.setup(log, message, args);
        replay(log);

        LoggedUtils.log(log, logLevel, message, args);
        verify(log);
    }
}
