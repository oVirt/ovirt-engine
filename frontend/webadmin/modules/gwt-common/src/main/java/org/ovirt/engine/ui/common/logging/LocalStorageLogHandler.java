package org.ovirt.engine.ui.common.logging;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.utils.AddOnlyRingBuffer;
import org.ovirt.engine.ui.common.utils.AddOnlyRingBuffer.LinearBuffer;

import com.google.gwt.logging.client.TextLogFormatter;
import com.google.inject.Inject;

/**
 * Log handler that uses {@link ClientStorage} for persisting log records.
 */
public class LocalStorageLogHandler extends Handler implements LinearBuffer<String>, ClientLogProvider {

    private static final String KEY_LOG_PREFIX = "Log_"; //$NON-NLS-1$
    private static final String KEY_HEAD = "LogHead"; //$NON-NLS-1$
    private static final String KEY_SIZE = "LogSize"; //$NON-NLS-1$

    // Maximum number of log records to keep in underlying storage
    private static final int LOG_CAPACITY = 100;

    private final ClientStorage clientStorage;
    private final AddOnlyRingBuffer<String> logBuffer;

    private boolean active = false;

    @Inject
    public LocalStorageLogHandler(ClientStorage clientStorage) {
        this.clientStorage = clientStorage;
        this.logBuffer = new AddOnlyRingBuffer<>(LOG_CAPACITY, this);
    }

    public void init() {
        setFormatter(new TextLogFormatter(true));
        setLevel(Level.ALL);
        setActive(true);
    }

    public void setActive(boolean active) {
        this.active = active;

        if (active) {
            // Read logBuffer state
            int newHead = readInt(KEY_HEAD, 0);
            int newSize = readInt(KEY_SIZE, 0);
            logBuffer.reset(newHead, newSize);
        }
    }

    int readInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(clientStorage.getLocalItem(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    void writeInt(String key, int value) {
        clientStorage.setLocalItem(key, String.valueOf(value));
    }

    @Override
    public void publish(LogRecord record) {
        if (active && isLoggable(record)) {
            // Add message to logBuffer
            String message = getFormatter().format(record);
            logBuffer.add(message);

            // Write logBuffer state
            writeInt(KEY_HEAD, logBuffer.head());
            writeInt(KEY_SIZE, logBuffer.size());
        }
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        return super.isLoggable(record) && clientStorage.isWebStorageAvailable();
    }

    @Override
    public void flush() {
        // No action needed
    }

    @Override
    public void close() {
        // No action needed
    }

    @Override
    public void write(int index, String element) {
        clientStorage.setLocalItem(getLogRecordKey(index), element);
    }

    @Override
    public String read(int index) {
        return clientStorage.getLocalItem(getLogRecordKey(index));
    }

    @Override
    public List<String> getLogRecords() {
        return logBuffer.list();
    }

    String getLogRecordKey(int index) {
        return KEY_LOG_PREFIX + index;
    }

}
