package org.ovirt.engine.ui.uicommonweb.models.userportal;

import org.ovirt.engine.ui.uicommonweb.models.HasConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;

public interface UserSelectedDisplayProtocolManager {
    void setSelectedProtocol(ConsoleProtocol protocol, HasConsoleModel item);
    ConsoleProtocol resolveSelectedProtocol(HasConsoleModel item);
}
