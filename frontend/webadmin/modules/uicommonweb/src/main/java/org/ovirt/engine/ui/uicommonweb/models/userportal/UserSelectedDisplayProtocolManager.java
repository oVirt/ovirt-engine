package org.ovirt.engine.ui.uicommonweb.models.userportal;

public interface UserSelectedDisplayProtocolManager {
    void setSelectedProtocol(ConsoleProtocol protocol, UserPortalItemModel item);
    ConsoleProtocol resolveSelectedProtocol(UserPortalItemModel item);
}
