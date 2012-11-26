package org.ovirt.engine.ui.userportal.widget.basic;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.uicommon.ClientAgentType;
import org.ovirt.engine.ui.uicommonweb.models.userportal.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncConsoleModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjectorProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Inject;

public class ConsoleUtils {

    private Boolean spiceAvailable;
    private Boolean rdpAvailable;

    private static final String VNC_NOT_SUPPORTED_MESSAGE =
            ClientGinjectorProvider.instance().getApplicationConstants().vncNotSupportedMsg();

    private static final String BROWSER_NOT_SUPPORTED_MESSAGE =
            ClientGinjectorProvider.instance().getApplicationConstants().browserNotSupportedMsg();

    private final ClientAgentType clientAgentType;

    @Inject
    public ConsoleUtils(ClientAgentType clientAgentType) {
        this.clientAgentType = clientAgentType;
    }

    public boolean isSpiceAvailable() {
        if (spiceAvailable == null) {
            spiceAvailable =
                    clientAgentType.getBrowser().toLowerCase().contains("firefox") //$NON-NLS-1$
                            && isLinuxClient() ||
                            clientAgentType.getBrowser().toLowerCase().contains("explorer") //$NON-NLS-1$
                            && isWindowsClient();
            GWT.log("Determining if Spice console is available on current platform, result:" + spiceAvailable); //$NON-NLS-1$
        }
        return spiceAvailable;
    }

    private boolean isLinuxClient() {
        return clientAgentType.getOS().toLowerCase().contains("linux"); //$NON-NLS-1$
    }

    private boolean isWindowsClient() {
        return clientAgentType.getOS().toLowerCase().contains("windows"); //$NON-NLS-1$
    }

    public boolean isRDPAvailable() {
        if (rdpAvailable == null) {
            rdpAvailable =
                    (clientAgentType.getBrowser().toLowerCase().contains("explorer") && isWindowsClient()); //$NON-NLS-1$
            GWT.log("Determining if RDP console is available on current platform, result:" + rdpAvailable); //$NON-NLS-1$
        }
        return rdpAvailable;
    }

    public boolean canOpenSpiceConsole(UserPortalItemModel item) {
        if (item.getIsPool() || !isSpiceAvailable())
            return false;

        VM vm = ((VM) item.getEntity());

        if (vm.getDisplayType().equals(DisplayType.qxl) &&
                item.getDefaultConsole().getConnectCommand().getIsAvailable() &&
                item.getDefaultConsole().getConnectCommand().getIsExecutionAllowed()) {
            return true;
        }

        return false;
    }

    public boolean canOpenRDPConsole(UserPortalItemModel item) {
        if (item.getIsPool() || !isRDPAvailable())
            return false;

        if (item.getHasAdditionalConsole() &&
                item.getAdditionalConsole().getConnectCommand().getIsAvailable() &&
                item.getAdditionalConsole().getConnectCommand().getIsExecutionAllowed()) {
            return true;
        }

        return false;
    }

    public String determineProtocolMessage(UserPortalItemModel item) {
        if (item.getIsPool()) {
            return ""; //$NON-NLS-1$
        }

        if (!(isRDPAvailable() || isSpiceAvailable())) {
            return BROWSER_NOT_SUPPORTED_MESSAGE;
        }

        boolean isSpice = item.getDefaultConsole() instanceof SpiceConsoleModel && isSpiceAvailable();
        boolean isRdp = item.getHasAdditionalConsole() && isRDPAvailable();

        if (!isSpice && !isRdp) {
            return VNC_NOT_SUPPORTED_MESSAGE;
        }

        return ""; //$NON-NLS-1$
    }

    public ConsoleProtocol determineConnectionProtocol(UserPortalItemModel item) {
        if (item.getIsPool()) {
            return null;
        }

        ConsoleProtocol selectedProtocol = item.getSelectedProtocol();

        if (item.getHasAdditionalConsole() && isRDPAvailable() && selectedProtocol.equals(ConsoleProtocol.RDP)) {
            return ConsoleProtocol.RDP;
        } else if (item.getDefaultConsole() instanceof SpiceConsoleModel && isSpiceAvailable() &&
                selectedProtocol.equals(ConsoleProtocol.SPICE)) {
            return ConsoleProtocol.SPICE;
        } else if (item.getDefaultConsole() instanceof VncConsoleModel) {
            return ConsoleProtocol.VNC;
        }

        return null;
    }

    public boolean canShowConsole(ConsoleProtocol selectedProtocol, UserPortalItemModel item) {
        if (selectedProtocol == null) {
            return false;
        }

        boolean isSpiceAvailable =
                selectedProtocol.equals(ConsoleProtocol.SPICE) && canOpenSpiceConsole(item);
        boolean isRdpAvailable =
                (selectedProtocol.equals(ConsoleProtocol.RDP) && canOpenRDPConsole(item));
        boolean isVncAvailable =
                (selectedProtocol.equals(ConsoleProtocol.VNC));

        return isSpiceAvailable || isRdpAvailable || isVncAvailable;
    }

    /**
     * The ctrl+alt+del is enabled for all OS except windows newer than 7
     * @return false if and only if the client OS type is Windows 7 or newer otherwise returns true
     */
    public boolean isCtrlAltDelEnabled() {
        if (!isWindowsClient()) {
            return true;
        }

        float ntVersion = extractNtVersion(getUserAgentString());

        // For Windows 7 and Windows Server 2008 R2 it is NT 6.1
        // For Windows 8 and Windows Server 2012 it is NT 6.2
        // The passing of ctrl+alt+del is enabled only on windows older
        // than Windows 7, so NT less than 6.1
        if (ntVersion >= 6.1f) {
            return false;
        }

        return true;
    }

    private float extractNtVersion(String userAgentType) {
        RegExp pattern = RegExp.compile(".*windows nt (\\d+\\.\\d+).*"); //$NON-NLS-1$
        MatchResult matcher = pattern.exec(userAgentType.toLowerCase());
        boolean matchFound = (matcher != null);
        if (matchFound) {
            return Float.parseFloat(matcher.getGroup(1));
        }

        return -1;
    }

    /**
     * Returns true if the smartcard is enabled for the specific VM entity (edit VM popup)
     */
    public boolean isSmartcardGloballyEnabled(UserPortalItemModel item) {
        ConsoleModel consoleModel = item.getDefaultConsole();
        if (consoleModel instanceof SpiceConsoleModel) {
            return consoleModel.getEntity() == null ? false : consoleModel.getEntity().isSmartcardEnabled();
        }

        return false;
    }

    /**
     * Returns true if the smartcard is locally disabled from the edit console options popup
     */
    public boolean isSmartcardEnabledOverriden(UserPortalItemModel item) {
        ConsoleModel consoleModel = item.getDefaultConsole();
        if (consoleModel instanceof SpiceConsoleModel) {
            return ((SpiceConsoleModel) consoleModel).getspice().isSmartcardEnabledOverridden();
        }

        return false;
    }

    public native String getUserAgentString() /*-{
                                              var userAgent = navigator.userAgent;
                                              return userAgent;
                                              }-*/;
}
