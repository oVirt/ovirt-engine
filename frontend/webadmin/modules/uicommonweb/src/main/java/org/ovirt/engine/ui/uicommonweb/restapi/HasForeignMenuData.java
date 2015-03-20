package org.ovirt.engine.ui.uicommonweb.restapi;

import org.ovirt.engine.core.compat.Guid;

public interface HasForeignMenuData {
    String getSessionId();
    void setSessionId(String sessionId);

    Guid getVmId();
    void setVmId(Guid vmId);

    String getEngineHost();
    void setEngineHost(String host);

    boolean isAdmin();
    void setAdmin(boolean admin);
}
