package org.ovirt.engine.core.common.test;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class InMemoryLoggingHandler extends Handler {
    private List<LogRecord> records = new LinkedList<>();

    @Override
    public void close() throws SecurityException {
    }

    @Override
    public void flush() {
    }

    @Override
    public void publish(LogRecord record) {
        records.add(record);
    }

    public List<LogRecord> getLogRecords() {
        return records;
    }
}
