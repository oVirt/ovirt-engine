package org.ovirt.engine.ui.uicommonweb;

@SuppressWarnings("unused")
public interface ILogger {
    void debug(String message);

    void info(String message);

    void error(String message, RuntimeException ex);

    void warn(String message);
}
