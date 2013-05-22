package org.ovirt.engine.ui.common.utils;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.uicommon.ClientAgentType;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;
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

public class ConsoleUtilsImpl implements ConsoleUtils {

    private Boolean rdpAvailable;

    private final CommonApplicationConstants constants;
    private final ClientAgentType clientAgentType;
    private final Configurator configurator;

    @Inject
    public ConsoleUtilsImpl(Configurator configurator, CommonApplicationConstants constants) {
        this.configurator= configurator;
        this.constants = constants;
        this.clientAgentType = new ClientAgentType();
    }

    //TODO consider refactoring it to use one parameter only if possible
    @Override
    public boolean canShowConsole(ConsoleProtocol selectedProtocol, HasConsoleModel item) {
        if (selectedProtocol == null || item == null) {
            return false;
        }

        boolean isSpiceAvailable =
                selectedProtocol.equals(ConsoleProtocol.SPICE) && canOpenSpiceConsole(item);
        boolean isRdpAvailable =
                selectedProtocol.equals(ConsoleProtocol.RDP) && canOpenRDPConsole(item);
        boolean isVncAvailable =
                selectedProtocol.equals(ConsoleProtocol.VNC) && canOpenVNCConsole(item);

        return isSpiceAvailable || isRdpAvailable || isVncAvailable;
    }

    @Override
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

    @Override
    public boolean isRDPAvailable() {
        if (rdpAvailable == null) {
            rdpAvailable = configurator.isClientWindows();
            GWT.log("Determining if RDP console is available on current platform, result:" + rdpAvailable); //$NON-NLS-1$
        }
        return rdpAvailable;
    }

    @Override
    public boolean canOpenSpiceConsole(HasConsoleModel item) {
        if (item.isPool()) {
            return false;
        }

        if (item.getDefaultConsoleModel().getConnectCommand().getIsAvailable() &&
            item.getDefaultConsoleModel().getConnectCommand().getIsExecutionAllowed()) {
            return true;
        }

        return false;
    }

    @Override
    public boolean canOpenVNCConsole(HasConsoleModel item) {
        if (item.isPool())
            return false;

        if (item.getDefaultConsoleModel().getConnectCommand().getIsAvailable() &&
            item.getDefaultConsoleModel().getConnectCommand().getIsExecutionAllowed()) {
            return true;
        }

        return false;
    }

    @Override
    public String determineProtocolMessage(HasConsoleModel item) {
        if (item.isPool()) {
            return ""; //$NON-NLS-1$
        }

        if (!(isRDPAvailable())) {
            return constants.browserNotSupportedMsg();
        }

        boolean isSpice = item.getDefaultConsoleModel() instanceof SpiceConsoleModel;
        boolean isRdp = item.getAdditionalConsoleModel() != null && isRDPAvailable();

        if (!isSpice && !isRdp) {
            return constants.vncNotSupportedMsg();
        }

        return ""; //$NON-NLS-1$
    }

    @Override
    public ConsoleProtocol determineConnectionProtocol(HasConsoleModel item) {
        if (item == null || item.isPool()) {
            return null;
        }

        ConsoleProtocol selectedProtocol = item.getUserSelectedProtocol();

        if (item.getAdditionalConsoleModel() != null && isRDPAvailable() && ConsoleProtocol.RDP.equals(selectedProtocol)) {
            return ConsoleProtocol.RDP;
        } else if (item.getDefaultConsoleModel() instanceof SpiceConsoleModel &&
                ConsoleProtocol.SPICE.equals(selectedProtocol)) {
            return ConsoleProtocol.SPICE;
        } else if (item.getDefaultConsoleModel() instanceof VncConsoleModel) {
            return ConsoleProtocol.VNC;
        }

        return null;
    }

    /**
     * The ctrl+alt+del is enabled for all OS except windows newer than 7
     * @return false if and only if the client OS type is Windows 7 or newer otherwise returns true
     */
    @Override
    public boolean isCtrlAltDelEnabled() {
        if (!configurator.isClientWindows()) {
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
    @Override
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
    @Override
    public boolean isSmartcardEnabledOverriden(HasConsoleModel item) {
        ConsoleModel consoleModel = item.getDefaultConsoleModel();
        if (consoleModel instanceof SpiceConsoleModel) {
            return ((SpiceConsoleModel) consoleModel).getspice().isSmartcardEnabledOverridden();
        }

        return false;
    }

    @Override
    public boolean isWanOptionsAvailable(HasConsoleModel model) {
        boolean spiceAvailable =
                model.getDefaultConsoleModel() instanceof SpiceConsoleModel;
        boolean isWindowsVm = AsyncDataProvider.isWindowsOsType(model.getVM().getOs());
        boolean spiceGuestAgentInstalled = model.getVM().getSpiceDriverVersion() != null;

        return spiceAvailable && isWindowsVm && spiceGuestAgentInstalled;
    }

    @Override
    public boolean isSpiceProxyDefined() {
        return configurator.isSpiceProxyDefined();
    }

    @Override
    public boolean isBrowserPluginSupported(ConsoleProtocol protocol) {
        switch (protocol) {
        case SPICE:
            if ((clientAgentType.os.equalsIgnoreCase("Windows")) //$NON-NLS-1$
                    && (clientAgentType.browser.equalsIgnoreCase("Explorer")) //$NON-NLS-1$
                    && (clientAgentType.version >= 7.0)) {
                return true;
            } else if ((clientAgentType.os.equalsIgnoreCase("Linux")) //$NON-NLS-1$
                    && (clientAgentType.browser.equalsIgnoreCase("Firefox")) //$NON-NLS-1$
                    && (clientAgentType.version >= 2.0)) {
                return true;
            }
            return false;
        case RDP:
            if ((clientAgentType.os.equalsIgnoreCase("Windows"))//$NON-NLS-1$
                    && (clientAgentType.browser.equalsIgnoreCase("Explorer"))//$NON-NLS-1$
                    && (clientAgentType.version >= 7.0)) {
                return true;
            }
            return false;
        default:
            return false;
        }
    }

    private native String getUserAgentString() /*-{
                                              var userAgent = navigator.userAgent;
                                              return userAgent;
                                              }-*/;
}
