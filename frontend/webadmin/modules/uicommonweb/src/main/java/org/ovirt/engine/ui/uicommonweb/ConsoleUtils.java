package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;

/**
 * Only console helper methods which don't need particular vm state.
 */
public interface ConsoleUtils {

    /**
     * The ctrl+alt+del is enabled for all OS except windows newer than 7
     * @return false if and only if the client OS type is Windows 7 or newer otherwise returns true
     */
    public boolean isCtrlAltDelEnabled();

    public boolean isBrowserPluginSupported(ConsoleProtocol protocol);

    public boolean isSpiceProxyDefined();

    public boolean isWebSocketProxyDefined();

}
