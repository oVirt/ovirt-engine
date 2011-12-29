package org.ovirt.engine.ui.webadmin.uicommon;

import java.util.logging.Logger;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpice;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;

public class SpiceInterfaceImpl implements ISpice {
    private static Logger logger = Logger.getLogger(SpiceInterfaceImpl.class
            .getName());

    private Event disconnectedEvent = new Event(
            SpiceConsoleModel.SpiceDisconnectedEventDefinition);
    private Event connectedEvent = new Event(
            SpiceConsoleModel.SpiceConnectedEventDefinition);
    private Event menuItemSelectedEvent = new Event(
            SpiceConsoleModel.SpiceMenuItemSelectedEventDefinition);
    private Version currentVersion = new Version(4, 4);
    private Version desiredVersion = new Version(4, 4);
    private int port;
    public String host;
    private boolean fullScreen;
    private String password;
    private int numberOfMonitors;
    private int usbListenPort;
    private boolean adminConsole;
    private String guestHostName;
    private int securePort;
    private String sslChanels;
    private String cipherSuite;
    private String hostSubject;
    private String trustStore;
    private String title;
    private String hotKey;
    private String[] localizedStrings;
    private String menu;
    private String guestID;
    private boolean noTaskMgrExecution;
    private boolean sendCtrlAltDelete;
    private boolean usbAutoShare;
    private String usbFilter;
    ClientAgentType cat = new ClientAgentType();

    public SpiceInterfaceImpl() {
        logger.severe("Instantiating GWT Spice Implementation");
    }

    @Override
    public Event getDisconnectedEvent() {
        return disconnectedEvent;
    }

    public void setDisconnectedEvent(Event disconnectedEvent) {
        this.disconnectedEvent = disconnectedEvent;
    }

    @Override
    public Event getConnectedEvent() {
        return connectedEvent;
    }

    public void setConnectedEvent(Event connectedEvent) {
        this.connectedEvent = connectedEvent;
    }

    @Override
    public Event getMenuItemSelectedEvent() {
        return menuItemSelectedEvent;
    }

    public void setMenuItemSelectedEvent(Event menuItemSelectedEvent) {
        this.menuItemSelectedEvent = menuItemSelectedEvent;
    }

    @Override
    public Version getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(Version currentVersion) {
        this.currentVersion = currentVersion;
    }

    // This should be defined by UiCommon
    @Override
    public Version getDesiredVersion() {
        return desiredVersion;
    }

    @Override
    public void setDesiredVersion(Version desiredVersion) {
        this.desiredVersion = desiredVersion;
    }

    public String getDesiredVersionStr() {
        return desiredVersion.toString().replace('.', ',');
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    @Override
    public boolean getFullScreen() {
        return fullScreen;
    }

    @Override
    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int getNumberOfMonitors() {
        return numberOfMonitors;
    }

    @Override
    public void setNumberOfMonitors(int numberOfMonitors) {
        this.numberOfMonitors = numberOfMonitors;
    }

    @Override
    public int getUsbListenPort() {
        return usbListenPort;
    }

    @Override
    public void setUsbListenPort(int usbListenPort) {
        this.usbListenPort = usbListenPort;
    }

    public boolean isAdminConsole() {
        return adminConsole;
    }

    @Override
    public boolean getAdminConsole() {
        return adminConsole;
    }

    @Override
    public void setAdminConsole(boolean adminConsole) {
        this.adminConsole = adminConsole;
    }

    @Override
    public String getGuestHostName() {
        return guestHostName;
    }

    @Override
    public void setGuestHostName(String guestHostName) {
        this.guestHostName = guestHostName;
    }

    @Override
    public int getSecurePort() {
        return securePort & 0xffff;
    }

    @Override
    public void setSecurePort(int securePort) {
        this.securePort = securePort;
    }

    @Override
    public String getSslChanels() {
        return sslChanels;
    }

    @Override
    public void setSslChanels(String sslChanels) {
        this.sslChanels = sslChanels;
    }

    @Override
    public String getCipherSuite() {
        return cipherSuite;
    }

    @Override
    public void setCipherSuite(String cipherSuite) {
        this.cipherSuite = cipherSuite;
    }

    @Override
    public String getHostSubject() {
        return hostSubject;
    }

    @Override
    public void setHostSubject(String hostSubject) {
        this.hostSubject = hostSubject;
    }

    @Override
    public String getTrustStore() {
        return trustStore;
    }

    @Override
    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getHotKey() {
        return hotKey;
    }

    @Override
    public void setHotKey(String hotKey) {
        this.hotKey = hotKey;
    }

    @Override
    public String[] getLocalizedStrings() {
        return localizedStrings;
    }

    @Override
    public void setLocalizedStrings(String[] localizedStrings) {
        this.localizedStrings = localizedStrings;
    }

    @Override
    public String getMenu() {
        return menu;
    }

    @Override
    public void setMenu(String menu) {
        this.menu = menu;
    }

    @Override
    public String getGuestID() {
        return guestID;
    }

    @Override
    public void setGuestID(String guestID) {
        this.guestID = guestID;
    }

    @Override
    public boolean getNoTaskMgrExecution() {
        return noTaskMgrExecution;
    }

    @Override
    public void setNoTaskMgrExecution(boolean noTaskMgrExecution) {
        this.noTaskMgrExecution = noTaskMgrExecution;
    }

    @Override
    public boolean getSendCtrlAltDelete() {
        return sendCtrlAltDelete;
    }

    @Override
    public void setSendCtrlAltDelete(boolean sendCtrlAltDelete) {
        this.sendCtrlAltDelete = sendCtrlAltDelete;
    }

    @Override
    public boolean getUsbAutoShare() {
        return usbAutoShare;
    }

    @Override
    public void setUsbAutoShare(boolean usbAutoShare) {
        this.usbAutoShare = usbAutoShare;
    }

    @Override
    public String getUsbFilter() {
        return usbFilter;
    }

    @Override
    public void setUsbFilter(String usbFilter) {
        this.usbFilter = usbFilter;
    }

    @Override
    public void Connect() {
        logger.warning("Connecting via spice...");

        if ((cat.os.equalsIgnoreCase("Linux"))
                && (cat.browser.equalsIgnoreCase("Firefox"))) {
            connectNativelyViaXPI();
        } else if ((cat.os.equalsIgnoreCase("Windows"))
                && (cat.browser.equalsIgnoreCase("Explorer"))) {
            connectNativelyViaActiveX();
        }
    }

    public String getSpiceCabURL() {
        // According to the OS type, return the appropriate CAB URL
        if (cat.getPlatform().equalsIgnoreCase("win32")) {
            return WebAdminConfigurator.getSpiceBaseURL() + "SpiceX.cab";
        } else if (cat.getPlatform().equalsIgnoreCase("win64")) {
            return WebAdminConfigurator.getSpiceBaseURL() + "SpiceX_x64.cab";
        } else {
            return null;
        }
    }

    public String getSpiceObjectClassId() {
        // According to the OS type, return the appropriate (x64/x86) object
        // class ID
        if (cat.getPlatform().equalsIgnoreCase("win32")) {
            return "ACD6D89C-938D-49B4-8E81-DDBD13F4B48A";
        } else if (cat.getPlatform().equalsIgnoreCase("win64")) {
            return "ACD6D89C-938D-49B4-8E81-DDBD13F4B48B";
        } else {
            return null;
        }
    }

    public native String loadActiveX(String id, String codebase, String classId) /*-{
		var container = $wnd.document.createElement("div");
		container.innerHTML = '<object id="' + id + '" codebase="' + codebase
				+ '" classid="CLSID:' + classId
				+ '" width="0" height="0"></object>';
		container.style.width = "0px";
		container.style.height = "0px";
		container.style.position = "absolute";
		container.style.top = "0px";
		container.style.left = "0px";
		var target_element = $wnd.document.getElementsByTagName("body")[0];
		if (typeof (target_element) == "undefined" || target_element == null)
			return false;
		target_element.appendChild(container);
    }-*/;

    public native String loadXpi(String id) /*-{
		var container = $wnd.document.createElement("div");
		container.innerHTML = '<embed id="' + id
				+ '" type="application/x-spice" width=0 height=0/>';
		container.style.width = "0px";
		container.style.height = "0px";
		container.style.position = "absolute";
		container.style.top = "0px";
		container.style.left = "0px";
		var target_element = $wnd.document.getElementsByTagName("body")[0];
		if (typeof (target_element) == "undefined" || target_element == null)
			return false;
		target_element.appendChild(container);
    }-*/;

    public native void connectNativelyViaXPI() /*-{
		var pluginFound = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::detectXpiPlugin()();
		if (!pluginFound) {
			alert("Spice XPI addon was not found, please install Spice XPI addon first.");
			return;
		}

		var hostIp = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getHost()();
		var port = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getPort()();
		var fullScreen = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getFullScreen()();
		var password = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getPassword()();
		var numberOfMonitors = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getNumberOfMonitors()();
		var usbListenPort = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getUsbListenPort()();
		var adminConsole = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getAdminConsole()();
		var guestHostName = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getGuestHostName()();
		var securePort = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getSecurePort()();
		var sslChanels = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getSslChanels()();
		var cipherSuite = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getCipherSuite()();
		var hostSubject = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getHostSubject()();
		var trustStore = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getTrustStore()();
		var title = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getTitle()();
		var hotKey = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getHotKey()();
		var menu = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getMenu()();
		var guestID = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getGuestID()();
		var version = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getCurrentVersion()();
		var spiceCabURL = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getSpiceCabURL()();
		var spiceCabOjectClassId = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getSpiceObjectClassId()();
		var id = "SpiceX_" + guestHostName;
		var noTaskMgrExecution = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getNoTaskMgrExecution()();
		var sendCtrlAltDelete = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getSendCtrlAltDelete()();
		var usbAutoShare = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getUsbAutoShare()();
		var usbFilter = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getUsbFilter()();
		var disconnectedEvent = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getDisconnectedEvent()();
		var connectedEvent = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getConnectedEvent()();
		var menuItemSelectedEvent = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getMenuItemSelectedEvent()();
		var model = this;

		//        alert("Host IP: " + hostIp + ", port: " + port + ", fullScreen: "
		//              + fullScreen + ", password: " + password
		//              + ", numberOfMonitors: " + numberOfMonitors
		//              + ", Usb Listen Port: " + usbListenPort + ", Admin Console: "
		//              + adminConsole + ", Guest HostName: " + guestHostName
		//              + ", Secure Port: " + securePort + ", Ssl Chanels: "
		//              + sslChanels + ", cipherSuite: " + cipherSuite
		//              + ", Host Subject: " + hostSubject + ", Title: " + title
		//              + ", Hot Key: " + hotKey + ", Menu: " + menu + ", GuestID: "
		//              + guestID + ", version: " + version + ", Trust Store: "
		//              + trustStore);

		this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::loadXpi(Ljava/lang/String;)(id);
		var client = $wnd.document.getElementById(id);
		client.hostIP = hostIp;
		client.port = port;
		client.Title = title;
		client.dynamicMenu = "";
		client.fullScreen = fullScreen;
		client.Password = password;
		client.NumberOfMonitors = numberOfMonitors;
		client.UsbListenPort = usbListenPort;
		client.AdminConsole = adminConsole;
		client.SecurePort = securePort;
		if (sslChanels != null && sslChanels.length > 0)
			client.SSLChannels = sslChanels;
		client.GuestHostName = guestHostName;
		if (cipherSuite != null && cipherSuite.length > 0)
			client.CipherSuite = cipherSuite;
		if (hostSubject != null)
			client.HostSubject = hostSubject;
		if (trustStore != null)
			client.TrustStore = trustStore;
		client.HotKey = hotKey;
		client.NoTaskMgrExecution = noTaskMgrExecution;
		client.SendCtrlAltDelete = sendCtrlAltDelete;
		client.UsbAutoShare = usbAutoShare;
		client.connect();

		connectedEvent.@org.ovirt.engine.core.compat.Event::raise(Ljava/lang/Object;Lorg/ovirt/engine/core/compat/EventArgs;)(model, null);

		//since the 'ondisconnected' event doesn't work well in linux, we use polling instead:
		var checkConnectStatusIntervalID = setInterval(checkConnectStatus, 2000);
		function checkConnectStatus() {
			if (client.ConnectedStatus() >= 0) {
				clearInterval(checkConnectStatusIntervalID);
				var connectedStatus = client.ConnectedStatus();
				var errorCodeEventArgs = @org.ovirt.engine.ui.uicommonweb.models.vms.ErrorCodeEventArgs::new(I)(connectedStatus);
				disconnectedEvent.@org.ovirt.engine.core.compat.Event::raise(Ljava/lang/Object;Lorg/ovirt/engine/core/compat/EventArgs;)(model, errorCodeEventArgs);
			}
		}
    }-*/;

    public native boolean detectXpiPlugin() /*-{
		var pluginsFound = false;
		if (navigator.plugins && navigator.plugins.length > 0) {
			var daPlugins = ["Spice"];
			var pluginsAmount = navigator.plugins.length;
			for (counter = 0; counter < pluginsAmount; counter++) {
				var numFound = 0;
				for (namesCounter = 0; namesCounter < daPlugins.length; namesCounter++) {
					if ((navigator.plugins[counter].name
							.indexOf(daPlugins[namesCounter]) > 0)
							|| (navigator.plugins[counter].description
									.indexOf(daPlugins[namesCounter]) >= 0)) {
						numFound++;
					}
				}
				if (numFound == daPlugins.length) {
					pluginsFound = true;
					break;
				}
			}

			return pluginsFound;
		}
    }-*/;

    public native void connectNativelyViaActiveX() /*-{
		var hostIp = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getHost()();
		var port = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getPort()();
		var fullScreen = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getFullScreen()();
		var password = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getPassword()();
		var numberOfMonitors = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getNumberOfMonitors()();
		var usbListenPort = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getUsbListenPort()();
		var adminConsole = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getAdminConsole()();
		var guestHostName = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getGuestHostName()();
		var securePort = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getSecurePort()();
		var sslChanels = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getSslChanels()();
		var cipherSuite = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getCipherSuite()();
		var hostSubject = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getHostSubject()();
		var trustStore = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getTrustStore()();
		var title = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getTitle()();
		var hotKey = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getHotKey()();
		var menu = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getMenu()();
		var guestID = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getGuestID()();
		var version = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getCurrentVersion()();
		var spiceCabURL = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getSpiceCabURL()();
		var spiceCabOjectClassId = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getSpiceObjectClassId()();
		var noTaskMgrExecution = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getNoTaskMgrExecution()();
		var sendCtrlAltDelete = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getSendCtrlAltDelete()();
		var usbAutoShare = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getUsbAutoShare()();
		var usbFilter = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getUsbFilter()();
		var menu = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getMenu()();
		var disconnectedEvent = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getDisconnectedEvent()();
        var connectedEvent = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getConnectedEvent()();
		var menuItemSelectedEvent = this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::getMenuItemSelectedEvent()();
		var codebase = spiceCabURL + "#version=" + version;
		var model = this;
		var id = "SpiceX_" + guestHostName;

		//		alert("Host IP: " + hostIp + ", port: " + port + ", fullScreen: "
		//				+ fullScreen + ", password: " + password
		//				+ ", numberOfMonitors: " + numberOfMonitors
		//				+ ", Usb Listen Port: " + usbListenPort + ", Admin Console: "
		//				+ adminConsole + ", Guest HostName: " + guestHostName
		//				+ ", Secure Port: " + securePort + ", Ssl Chanels: "
		//				+ sslChanels + ", cipherSuite: " + cipherSuite
		//				+ ", Host Subject: " + hostSubject + ", Title: " + title
		//				+ ", Hot Key: " + hotKey + ", Menu: " + menu + ", GuestID: "
		//				+ guestID + ", version: " + version + ", Trust Store: "
		//				+ trustStore);

		// Create ActiveX object
		this.@org.ovirt.engine.ui.webadmin.uicommon.SpiceInterfaceImpl::loadActiveX(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(id,codebase,spiceCabOjectClassId);

		var client = $wnd.document.getElementById(id);
		client.attachEvent('onreadystatechange', onReadyStateChange);
		client.attachEvent('onmenuitemselected', onMenuItemSelected);

		tryToConnect();

		function tryToConnect() {
			if (client.readyState == 4) {
				try {
					client.style.width = "0px";
					client.style.height = "0px";
					client.hostIP = hostIp;
					client.port = port;
					client.Title = title;
					client.dynamicMenu = menu;
					client.fullScreen = fullScreen;
					client.Password = password;
					client.NumberOfMonitors = numberOfMonitors;
					client.UsbListenPort = usbListenPort;
					client.AdminConsole = adminConsole;
					client.SecurePort = securePort;
					if (sslChanels != null && sslChanels.length > 0)
						client.SSLChannels = sslChanels;
					client.GuestHostName = guestHostName;
					if (cipherSuite != null && cipherSuite.length > 0)
						client.CipherSuite = cipherSuite;
					if (hostSubject != null)
						client.HostSubject = hostSubject;
					if (trustStore != null)
						client.TrustStore = trustStore;
					client.HotKey = hotKey;
					client.NoTaskMgrExecution = noTaskMgrExecution;
					client.SendCtrlAltDelete = sendCtrlAltDelete;
					client.UsbAutoShare = usbAutoShare;
					client.attachEvent('ondisconnected', onDisconnected);

					client.connect();

					connectedEvent.@org.ovirt.engine.core.compat.Event::raise(Ljava/lang/Object;Lorg/ovirt/engine/core/compat/EventArgs;)(model, null);

				} catch (ex) {
					onDisconnected();
				}
			}
		}

		function onReadyStateChange() {
			tryToConnect();
		}

		function onDisconnected(errorCode) {
			var errorCodeEventArgs = @org.ovirt.engine.ui.uicommonweb.models.vms.ErrorCodeEventArgs::new(I)(errorCode);
			disconnectedEvent.@org.ovirt.engine.core.compat.Event::raise(Ljava/lang/Object;Lorg/ovirt/engine/core/compat/EventArgs;)(model, errorCodeEventArgs);
		}

		function onMenuItemSelected(itemId) {
			var spiceMenuItemEventArgs = @org.ovirt.engine.ui.uicommonweb.models.vms.SpiceMenuItemEventArgs::new(I)(itemId);
			menuItemSelectedEvent.@org.ovirt.engine.core.compat.Event::raise(Ljava/lang/Object;Lorg/ovirt/engine/core/compat/EventArgs;)(model, spiceMenuItemEventArgs);
		}
    }-*/;

    @Override
    public void Install() {
    }

    @Override
    public boolean getIsInstalled() {
        return true;
    }

    public static boolean isBrowserSupported() {
        ClientAgentType cat = new ClientAgentType();
        logger.finer("Determining whether browser [" + cat.browser
                + "], version [" + cat.version + "] on OS [" + cat.os
                + "] is supported by spice");

        if ((cat.os.equalsIgnoreCase("Windows"))
                && (cat.browser.equalsIgnoreCase("Explorer"))
                && (cat.version >= 7.0)) {
            return true;
        } else if ((cat.os.equalsIgnoreCase("Linux"))
                && (cat.browser.equalsIgnoreCase("Firefox"))
                && (cat.version >= 2.0)) {
            return true;
        }

        return false;
    }
}
