package org.ovirt.engine.ui.common.logging;

import java.util.List;

/**
 * Interface providing access to client-side log records.
 */
public interface ClientLogProvider {

    /**
     * Returns the list of log records as {@code String} values.
     */
    List<String> getLogRecords();

}
