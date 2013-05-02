package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.HasConsoleModel;

public interface ConsoleUtils {

    public boolean isRDPAvailable();

    public boolean canOpenSpiceConsole(HasConsoleModel item);
    public boolean canOpenVNCConsole(HasConsoleModel item);
    public boolean canOpenRDPConsole(HasConsoleModel item);

    public String determineProtocolMessage(HasConsoleModel item);
    public ConsoleProtocol determineConnectionProtocol(HasConsoleModel item);

    //TODO consider refactoring it to use one parameter only if possible
    public boolean canShowConsole(ConsoleProtocol selectedProtocol, HasConsoleModel item);

    /**
     * The ctrl+alt+del is enabled for all OS except windows newer than 7
     * @return false if and only if the client OS type is Windows 7 or newer otherwise returns true
     */
    public boolean isCtrlAltDelEnabled();

    /**
     * Returns true if the smartcard is enabled for the specific VM entity (edit VM popup)
     */
    public boolean isSmartcardGloballyEnabled(HasConsoleModel item);

    /**
     * Returns true if the smartcard is locally disabled from the edit console options popup
     */
    public boolean isSmartcardEnabledOverriden(HasConsoleModel item);

    public boolean isWanOptionsAvailable(HasConsoleModel item);

    public boolean isBrowserPluginSupported();

    public boolean isSpiceProxyDefined();
}
