package org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.uicommon.ClientAgentType;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class ConsoleUtils {

    private Boolean spiceAvailable;
    private Boolean rdpAvailable;

    private static final String VNC_NOT_SUPPORTED_MESSAGE =
            "VNC console access is not supported from the user portal.<br/>" +
                    "Please ask the administrator to configure this " +
                    "virtual machine to use SPICE for console access.";

    private static final String BROWSER_NOT_SUPPORTED_MESSAGE =
            "Your browser/platform does not support console opening";

    private final ClientAgentType clientAgentType;

    @Inject
    public ConsoleUtils(ClientAgentType clientAgentType) {
        this.clientAgentType = clientAgentType;
    }

    public boolean isSpiceAvailable() {
        if (spiceAvailable == null) {
            spiceAvailable =
                    clientAgentType.getBrowser().toLowerCase().contains("firefox")
                            && clientAgentType.getOS().toLowerCase().contains("linux") ||
                            clientAgentType.getBrowser().toLowerCase().contains("explorer")
                            && clientAgentType.getOS().toLowerCase().contains("windows");
            GWT.log("Determining if Spice console is available on current platform, result:" + spiceAvailable);
        }
        return spiceAvailable;
    }

    public boolean isRDPAvailable() {
        if (rdpAvailable == null) {
            rdpAvailable =
                    (clientAgentType.getBrowser().toLowerCase().contains("explorer") && clientAgentType.getOS()
                            .toLowerCase()
                            .contains("windows"));
            GWT.log("Determining if RDP console is available on current platform, result:" + rdpAvailable);
        }
        return rdpAvailable;
    }

    public boolean canOpenSpiceConsole(UserPortalItemModel item) {
        if (item.getIsPool() || !isSpiceAvailable())
            return false;

        VM vm = ((VM) item.getEntity());

        if (vm.getdisplay_type().equals(DisplayType.qxl) &&
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
            return "";
        }

        if (!(isRDPAvailable() || isSpiceAvailable())) {
            return BROWSER_NOT_SUPPORTED_MESSAGE;
        }

        boolean isSpice = item.getDefaultConsole() instanceof SpiceConsoleModel && isSpiceAvailable();
        boolean isRdp = item.getHasAdditionalConsole() && isRDPAvailable();

        if (!isSpice && !isRdp) {
            return VNC_NOT_SUPPORTED_MESSAGE;
        }

        return "";
    }

    public ConsoleProtocol determineDefaultProtocol(UserPortalItemModel item) {
        if (item.getIsPool()) {
            return null;
        }

        if (item.getDefaultConsole() instanceof SpiceConsoleModel && isSpiceAvailable()) {
            return ConsoleProtocol.SPICE;
        } else if (item.getHasAdditionalConsole() && isRDPAvailable()) {
            return ConsoleProtocol.RDP;
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

        return isSpiceAvailable || isRdpAvailable;
    }

}
