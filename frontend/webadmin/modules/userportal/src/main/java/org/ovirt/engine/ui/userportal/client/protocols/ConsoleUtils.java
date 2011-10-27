package org.ovirt.engine.ui.userportal.client.protocols;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.ui.uicommon.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommon.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.userportal.client.util.ClientAgentType;

import com.google.gwt.core.client.GWT;

public class ConsoleUtils {

	private static Boolean spiceAvailable;
	private static Boolean rdpAvailable;

	private static final String VNC_NOT_SUPPORTED_MESSAGE = "VNC console access is not supported from the user portal.<br/>" +
	 "Please ask the administrator to configure this " +
	 "virtual machine to use SPICE for console access.";

	private static final String BROWSER_NOT_SUPPORTED_MESSAGE = "Your browser/platform does not support console opening";
	
	public static boolean isSpiceAvailable() {
		if (spiceAvailable == null) {
			ClientAgentType cat = new ClientAgentType();
			spiceAvailable = cat.getBrowser().toLowerCase().contains("firefox") && cat.getOS().toLowerCase().contains("linux") ||
								cat.getBrowser().toLowerCase().contains("explorer") && cat.getOS().toLowerCase().contains("windows");
			GWT.log("Determining if Spice console is available on current platform, result:" + spiceAvailable);
		}
		return spiceAvailable;
	}

	public static boolean isRDPAvailable() {
		if (rdpAvailable == null) {
			ClientAgentType cat = new ClientAgentType();
			rdpAvailable = (cat.getBrowser().toLowerCase().contains("explorer") && cat.getOS().toLowerCase().contains("windows"));
			GWT.log("Determining if RDP console is available on current platform, result:" + rdpAvailable);
		}
		return rdpAvailable;
	}
	
	public static boolean canOpenSpiceConsole(UserPortalItemModel item) {
		if (item.getIsPool() || !isSpiceAvailable())
			return false;
		
		VM vm = ((VM)item.getEntity());
		
		if (vm.getdisplay_type().equals(DisplayType.qxl) &&
			item.getDefaultConsole().getConnectCommand().getIsAvailable() && 
			item.getDefaultConsole().getConnectCommand().getIsExecutionAllowed()) {
			return true;
		}
		
		return false;
	}

	public static boolean canOpenRDPConsole(UserPortalItemModel item) {
		if (item.getIsPool() || !isRDPAvailable())
			return false;
				
		if (item.getHasAdditionalConsole() &&
			item.getAdditionalConsole().getConnectCommand().getIsAvailable() && 
			item.getAdditionalConsole().getConnectCommand().getIsExecutionAllowed()) {
			return true;
		}
		
		return false;
	}	
	
	public static void determineDefaultProtocol(ProtocolOptionContainer container, UserPortalItemModel item) {
		if (!item.getIsPool()) {
			if (!(ConsoleUtils.isRDPAvailable() || ConsoleUtils.isSpiceAvailable())) {
				container.setProtocolMessage(BROWSER_NOT_SUPPORTED_MESSAGE);
				return;
			}
			
			if (item.getDefaultConsole() instanceof SpiceConsoleModel && ConsoleUtils.isSpiceAvailable()) {
				container.setProtocol(Protocol.SPICE);
			}
			else {
				if (item.getHasAdditionalConsole() && ConsoleUtils.isRDPAvailable()) {
					container.setProtocol(Protocol.RDP);
				}
				else {
					container.setProtocolMessage(VNC_NOT_SUPPORTED_MESSAGE);
				}
			}
		}
	}

}
