package org.ovirt.engine.ui.common.utils;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.uicommon.ClientAgentType;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.HasConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncConsoleModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Inject;

public class ConsoleUtils {

    private Boolean spiceAvailable;
    private Boolean rdpAvailable;

    private final String VNC_NOT_SUPPORTED_MESSAGE;
    private final String BROWSER_NOT_SUPPORTED_MESSAGE;

    private final ClientAgentType clientAgentType;

    @Inject
    public ConsoleUtils(ClientAgentType clientAgentType, CommonApplicationConstants constants) {
        this.clientAgentType = clientAgentType;

        VNC_NOT_SUPPORTED_MESSAGE = constants.vncNotSupportedMsg();
        BROWSER_NOT_SUPPORTED_MESSAGE = constants.browserNotSupportedMsg();
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

    public boolean canOpenSpiceConsole(HasConsoleModel item) {
        if (item.isPool() || !isSpiceAvailable())
            return false;

        VM vm = item.getVM();

        if (vm.getDisplayType().equals(DisplayType.qxl) &&
                item.getDefaultConsoleModel().getConnectCommand().getIsAvailable() &&
                item.getDefaultConsoleModel().getConnectCommand().getIsExecutionAllowed()) {
            return true;
        }

        return false;
    }

    public boolean canOpenRDPConsole(HasConsoleModel item) {
        if (item.isPool() || !isRDPAvailable())
            return false;

        if (item.getAdditionalConsoleModel() != null &&
                item.getAdditionalConsoleModel().getConnectCommand().getIsAvailable() &&
                item.getAdditionalConsoleModel().getConnectCommand().getIsExecutionAllowed()) {
            return true;
        }

        return false;
    }

    public String determineProtocolMessage(HasConsoleModel item) {
        if (item.isPool()) {
            return ""; //$NON-NLS-1$
        }

        if (!(isRDPAvailable() || isSpiceAvailable())) {
            return BROWSER_NOT_SUPPORTED_MESSAGE;
        }

        boolean isSpice = item.getDefaultConsoleModel() instanceof SpiceConsoleModel && isSpiceAvailable();
        boolean isRdp = item.getAdditionalConsoleModel() != null && isRDPAvailable();

        if (!isSpice && !isRdp) {
            return VNC_NOT_SUPPORTED_MESSAGE;
        }

        return ""; //$NON-NLS-1$
    }

    public ConsoleProtocol determineConnectionProtocol(HasConsoleModel item) {
        if (item.isPool()) {
            return null;
        }

        ConsoleProtocol selectedProtocol = item.getSelectedProtocol();

        if (item.getAdditionalConsoleModel() != null && isRDPAvailable() && selectedProtocol.equals(ConsoleProtocol.RDP)) {
            return ConsoleProtocol.RDP;
        } else if (item.getDefaultConsoleModel() instanceof SpiceConsoleModel && isSpiceAvailable() &&
                selectedProtocol.equals(ConsoleProtocol.SPICE)) {
            return ConsoleProtocol.SPICE;
        } else if (item.getDefaultConsoleModel() instanceof VncConsoleModel) {
            return ConsoleProtocol.VNC;
        }

        return null;
    }

    public boolean canShowConsole(ConsoleProtocol selectedProtocol, HasConsoleModel item) {
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
    public boolean isSmartcardGloballyEnabled(HasConsoleModel item) {
        ConsoleModel consoleModel = item.getDefaultConsoleModel();
        if (consoleModel instanceof SpiceConsoleModel) {
            return consoleModel.getEntity() == null ? false : consoleModel.getEntity().isSmartcardEnabled();
        }

        return false;
    }

    /**
     * Returns true if the smartcard is locally disabled from the edit console options popup
     */
    public boolean isSmartcardEnabledOverriden(HasConsoleModel item) {
        ConsoleModel consoleModel = item.getDefaultConsoleModel();
        if (consoleModel instanceof SpiceConsoleModel) {
            return ((SpiceConsoleModel) consoleModel).getspice().isSmartcardEnabledOverridden();
        }

        return false;
    }

    public boolean isWanOptionsAvailable(HasConsoleModel model) {
        boolean spiceAvailable =
                model.getDefaultConsoleModel() instanceof SpiceConsoleModel && isSpiceAvailable();
        boolean isWindowsVm = model.getVM().getOs().isWindows();
        boolean spiceGuestAgentInstalled = model.getVM().getSpiceDriverVersion() != null;

        return spiceAvailable && isWindowsVm && spiceGuestAgentInstalled;
    }

    public boolean isSpiceProxyDefined() {
        String spiceProxy = (String) AsyncDataProvider.GetConfigValuePreConverted(ConfigurationValues.SpiceProxyDefault);
        return spiceProxy != null && !"".equals(spiceProxy); //$NON-NLS-1$
    }

    public native String getUserAgentString() /*-{
                                              var userAgent = navigator.userAgent;
                                              return userAgent;
                                              }-*/;
}
