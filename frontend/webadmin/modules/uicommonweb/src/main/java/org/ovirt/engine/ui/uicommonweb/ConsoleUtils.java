package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;

/**
 * Only console helper methods which don't need particular vm state.
 */
public interface ConsoleUtils {

    public boolean isBrowserPluginSupported(ConsoleProtocol protocol);

    public boolean isSpiceProxyDefined();

    public boolean isWebSocketProxyDefined();

    public String getRemapCtrlAltDelHotkey();

}
