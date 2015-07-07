package org.ovirt.engine.api.extensions.logger;

import java.util.logging.LogRecord;

import org.ovirt.engine.api.extensions.ExtKey;
import org.ovirt.engine.api.extensions.ExtUUID;

/**
 * Log extension interface. Permitted to process logging records of application.
 */
public class Logger {
    /**
     * Invoke keys.
     */
    public static class InvokeKeys {
        /** Log Record. */
        public static final ExtKey LOG_RECORD = new ExtKey("LOGGER_LOG_RECORD", LogRecord.class, "c5468087-0f65-46ea-a0c2-23777dfada6f");
    }

    /**
     * Invoke commands.
     */
    public static class InvokeCommands {
        /** Publish LogRecord. */
        public static final ExtUUID PUBLISH = new ExtUUID("LOGGER_PUBLISH", "69f6fc51-71d8-4ae5-a49e-1e00ef55a314");

        /** Close the logger. */
        public static final ExtUUID CLOSE = new ExtUUID("LOGGER_CLOSE", "101c7d96-d1e2-4ea6-aaad-b89343337e2b");

        /** Close the logger. */
        public static final ExtUUID FLUSH = new ExtUUID("LOGGER_FLUSH", "0a330097-24ef-4f94-907f-7a119c7dd5d3");
    }
}
