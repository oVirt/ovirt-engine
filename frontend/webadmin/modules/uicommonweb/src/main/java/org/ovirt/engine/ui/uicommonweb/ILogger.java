package org.ovirt.engine.ui.uicommonweb;

@SuppressWarnings("unused")
public interface ILogger
{
    void Debug(String message);

    void Info(String message);

    void Error(String message, RuntimeException ex);

    void Warn(String message);
}
