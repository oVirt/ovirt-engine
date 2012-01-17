package org.ovirt.engine.core.utils;

public interface ISession {
    void SetSessionDataBySessionId(String sessionId, String key, Object value);

    void SetSessionData(String key, Object value);
}
