package org.ovirt.engine.ui.userportal.client.uicommonext;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommon.models.vms.ISpice;
import org.ovirt.engine.ui.uicommon.models.vms.SpiceConsoleModel;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.ui.userportal.client.util.ClientAgentType;

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

	public Event getMenuItemSelectedEvent() {
		return menuItemSelectedEvent;
	}

	public void setMenuItemSelectedEvent(Event menuItemSelectedEvent) {
		this.menuItemSelectedEvent = menuItemSelectedEvent;
	}

	public Version getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(Version currentVersion) {
		this.currentVersion = currentVersion;
	}

	// This should be defined by UiCommon
	public Version getDesiredVersion() {
		return desiredVersion;
	}

	public void setDesiredVersion(Version desiredVersion) {
		this.desiredVersion = desiredVersion;
	}
	
	public String getDesiredVersionStr() {
        return desiredVersion.toString().replace('.', ',');
    }

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public boolean isFullScreen() {
		return fullScreen;
	}

	public boolean getFullScreen() {
		return fullScreen;
	}

	public void setFullScreen(boolean fullScreen) {
		this.fullScreen = fullScreen;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getNumberOfMonitors() {
		return numberOfMonitors;
	}

	public void setNumberOfMonitors(int numberOfMonitors) {
		this.numberOfMonitors = numberOfMonitors;
	}

	public int getUsbListenPort() {
		return usbListenPort;
	}

	public void setUsbListenPort(int usbListenPort) {
		this.usbListenPort = usbListenPort;
	}

	public boolean isAdminConsole() {
		return adminConsole;
	}

	public boolean getAdminConsole() {
		return adminConsole;
	}

	public void setAdminConsole(boolean adminConsole) {
		this.adminConsole = adminConsole;
	}

	public String getGuestHostName() {
		return guestHostName;
	}

	public void setGuestHostName(String guestHostName) {
		this.guestHostName = guestHostName;
	}

	public int getSecurePort() {
		return securePort & 0xffff;
	}

	public void setSecurePort(int securePort) {
		this.securePort = securePort;
	}

	public String getSslChanels() {
		return sslChanels;
	}

	public void setSslChanels(String sslChanels) {
		this.sslChanels = sslChanels;
	}

	public String getCipherSuite() {
		return cipherSuite;
	}

	public void setCipherSuite(String cipherSuite) {
		this.cipherSuite = cipherSuite;
	}

	public String getHostSubject() {
		return hostSubject;
	}

	public void setHostSubject(String hostSubject) {
		this.hostSubject = hostSubject;
	}

	public String getTrustStore() {
		return trustStore;
	}

	public void setTrustStore(String trustStore) {
		this.trustStore = trustStore;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getHotKey() {
		return hotKey;
	}

	public void setHotKey(String hotKey) {
		this.hotKey = hotKey;
	}

	public String[] getLocalizedStrings() {
		return localizedStrings;
	}

	public void setLocalizedStrings(String[] localizedStrings) {
		this.localizedStrings = localizedStrings;
	}

	public String getMenu() {
		return menu;
	}

	public void setMenu(String menu) {
		this.menu = menu;
	}

	public String getGuestID() {
		return guestID;
	}

	public void setGuestID(String guestID) {
		this.guestID = guestID;
	}
	
	public boolean getNoTaskMgrExecution() {
		return noTaskMgrExecution;
	}

	public void setNoTaskMgrExecution(boolean noTaskMgrExecution) {
		this.noTaskMgrExecution = noTaskMgrExecution;
	}
	
	public boolean getSendCtrlAltDelete() {
		return sendCtrlAltDelete;
	}

	public void setSendCtrlAltDelete(boolean sendCtrlAltDelete) {
		this.sendCtrlAltDelete = sendCtrlAltDelete;
	}
	
	public boolean getUsbAutoShare() {
		return usbAutoShare;
	}

	public void setUsbAutoShare(boolean usbAutoShare) {
		this.usbAutoShare = usbAutoShare;
	}
	
	public String getUsbFilter() {
        return usbFilter;
    }

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
			return GWT.getModuleBaseURL() + "SpiceX.cab";
		} else if (cat.getPlatform().equalsIgnoreCase("win64")) {
			return GWT.getModuleBaseURL() + "SpiceX_x64.cab";
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
		var container=$wnd.document.createElement("div");
		container.innerHTML ='<object id="'+id+'" codebase="'+codebase+'" classid="CLSID:'+classId+'" width="0" height="0"></object>';
		container.style.width="0px";
		container.style.height="0px";
		container.style.position="absolute";
		container.style.top="0px";
		container.style.left="0px";
		var target_element=$wnd.document.getElementsByTagName("body")[0];
		if (typeof(target_element)=="undefined"||target_element==null) return false;
			target_element.appendChild(container);
	}-*/;

	public native String loadXpi(String id) /*-{
		var container=document.createElement("div");
		container.innerHTML ='<embed id="'+id+'" type="application/x-spice" width=0 height=0/>';
		container.style.width="0px";
		container.style.height="0px";
		container.style.position="absolute";
		container.style.top="0px";
		container.style.left="0px";
		var target_element=document.getElementsByTagName("body")[0];
		if (typeof(target_element)=="undefined"||target_element==null) return false;
			target_element.appendChild(container);
	}-*/;

	public native void connectNativelyViaXPI() /*-{
		var pluginFound = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::detectXpiPlugin()();
		if (!pluginFound) {
			alert("Spice XPI addon was not found, please install Spice XPI addon first.");
			return;
		}

		var hostIp = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getHost()();
		var port = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getPort()();
		var fullScreen = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getFullScreen()();
		var password = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getPassword()();
		var numberOfMonitors = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getNumberOfMonitors()();
		var usbListenPort = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getUsbListenPort()();
		var adminConsole = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getAdminConsole()();
		var guestHostName = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getGuestHostName()();
		var securePort = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getSecurePort()();
		var sslChanels = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getSslChanels()();
		var cipherSuite = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getCipherSuite()();
		var hostSubject = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getHostSubject()();
		var trustStore = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getTrustStore()();
		var title = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getTitle()();
		var hotKey = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getHotKey()();
		var menu = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getMenu()();
		var guestID = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getGuestID()();
		var version = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getCurrentVersion()();
		var spiceCabURL = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getSpiceCabURL()();
		var spiceCabOjectClassId = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getSpiceObjectClassId()();
		var id = "SpiceX_" + guestHostName;
		var noTaskMgrExecution = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getNoTaskMgrExecution()();
		var sendCtrlAltDelete = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getSendCtrlAltDelete()();
		var usbAutoShare = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getUsbAutoShare()();
		var usbFilter = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getUsbFilter()();
		var disconnectedEvent = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getDisconnectedEvent()();
		var connectedEvent = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getConnectedEvent()();
		var model = this;

		//alert("Host IP ["+hostIp+"], port ["+port+"], fullScreen ["+fullScreen+"], password ["+password+"], numberOfMonitors ["+numberOfMonitors+"], Usb Listen Port ["+usbListenPort+"], Admin Console ["+adminConsole+"], Guest HostName ["+guestHostName+"], Secure Port ["+securePort+"], Ssl Chanels ["+sslChanels+"], cipherSuite ["+cipherSuite+"], Host Subject ["+hostSubject+"], Title [" + title+"], Hot Key ["+hotKey+"], Menu ["+menu+"], GuestID [" + guestID+"], version ["+version+"]");
		this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::loadXpi(Ljava/lang/String;)(id);
		var client = document.getElementById(id);
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
		if(sslChanels != null && sslChanels.length > 0)
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
		client.SetUsbFilter(usbFilter);
		client.connect();

		connectedEvent.@org.ovirt.engine.core.compat.Event::raise(Ljava/lang/Object;Lorg/ovirt/engine/core/compat/EventArgs;)(model, null);

		//since the 'ondisconnected' event doesn't work well in linux, we use polling instead:
		var checkConnectStatusIntervalID = setInterval(checkConnectStatus, 2000);  
		function checkConnectStatus() {
			if (client.ConnectedStatus() >= 0) {
				clearInterval(checkConnectStatusIntervalID);				
				
                var errorCodeEventArgs = @org.ovirt.engine.ui.uicommon.models.vms.ErrorCodeEventArgs::new(I)(client.ConnectedStatus());  
				disconnectedEvent.@org.ovirt.engine.core.compat.Event::raise(Ljava/lang/Object;Lorg/ovirt/engine/core/compat/EventArgs;)(model, errorCodeEventArgs);
				
                // Refresh grid on disconnect (to re-enable console button)
                var gridRefreshManager = @org.ovirt.engine.ui.userportal.client.components.GridRefreshManager::getInstance()();           
                gridRefreshManager.@org.ovirt.engine.ui.userportal.client.components.GridRefreshManager::refreshGrids()();    
			}
		}
	}-*/;

	public native boolean detectXpiPlugin() /*-{
		var pluginsFound = false;
		if (navigator.plugins && navigator.plugins.length > 0) {
			var daPlugins = ["Spice"];
			var pluginsAmount = navigator.plugins.length;
			for (counter=0; counter < pluginsAmount; counter++) {
				var numFound = 0;
				for (namesCounter=0;namesCounter < daPlugins.length;namesCounter++) {
					if ( (navigator.plugins[counter].name.indexOf(daPlugins[namesCounter]) > 0 ) ||
			     		(navigator.plugins[counter].description.indexOf(daPlugins[namesCounter]) >= 0) ) {
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
		var hostIp = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getHost()();
		var port = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getPort()();
		var fullScreen = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getFullScreen()();
		var password = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getPassword()();
		var numberOfMonitors = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getNumberOfMonitors()();
		var usbListenPort = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getUsbListenPort()();
		var adminConsole = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getAdminConsole()();
		var guestHostName = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getGuestHostName()();
		var securePort = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getSecurePort()();
		var sslChanels = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getSslChanels()();
		var cipherSuite = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getCipherSuite()();
		var hostSubject = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getHostSubject()();
		var trustStore = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getTrustStore()();
		var title = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getTitle()();
		var hotKey = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getHotKey()();
		var menu = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getMenu()();
		var guestID = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getGuestID()();
		var version = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getDesiredVersionStr()();
		var spiceCabURL = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getSpiceCabURL()();
		var spiceCabOjectClassId = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getSpiceObjectClassId()();
		var noTaskMgrExecution = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getNoTaskMgrExecution()();
		var sendCtrlAltDelete = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getSendCtrlAltDelete()();
		var usbAutoShare = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getUsbAutoShare()();
		var usbFilter = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getUsbFilter()();
		var disconnectedEvent = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getDisconnectedEvent()();
		var connectedEvent = this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::getConnectedEvent()();
		var codebase = spiceCabURL + "#version=" + version;
		var model = this;
		var id = "SpiceX_" + guestHostName;		
		//alert("Host IP ["+hostIp+"], port ["+port+"], fullScreen ["+fullScreen+"], password ["+password+"], numberOfMonitors ["+numberOfMonitors+"], Usb Listen Port ["+usbListenPort+"], Admin Console ["+adminConsole+"], Guest HostName ["+guestHostName+"], Secure Port ["+securePort+"], Ssl Chanels ["+sslChanels+"], cipherSuite ["+cipherSuite+"], Host Subject ["+hostSubject+"], Title [" + title+"], Hot Key ["+hotKey+"], Menu ["+menu+"], GuestID [" + guestID+"], version ["+version+"]");
		//alert("Trust Store ["+trustStore+"]");
		
		this.@org.ovirt.engine.ui.userportal.client.uicommonext.SpiceInterfaceImpl::loadActiveX(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(id,codebase,spiceCabOjectClassId);
		
		var client = $wnd.document.getElementById(id);
        client.attachEvent('onreadystatechange', onReadyStateChange); 
        tryToConnect();        
		
		function tryToConnect() {
		    if (client.readyState == 4) {
    		    try {                     
                    client.style.width = "0px";
                    client.style.height = "0px";
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
                    if(sslChanels != null && sslChanels.length > 0)
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
                    client.SetUsbFilter(usbFilter);
                    
                    client.attachEvent('ondisconnected', onDisconnected);
                    client.connect();

                    connectedEvent.@org.ovirt.engine.core.compat.Event::raise(Ljava/lang/Object;Lorg/ovirt/engine/core/compat/EventArgs;)(model, null);
                }
                catch (ex) {
                    onDisconnected();
                }
            }
		}
		
		function onReadyStateChange() {
		    tryToConnect();            
		}
		
		function onDisconnected(errorCode) {
            var errorCodeEventArgs = @org.ovirt.engine.ui.uicommon.models.vms.ErrorCodeEventArgs::new(I)(errorCode);          
            disconnectedEvent.@org.ovirt.engine.core.compat.Event::raise(Ljava/lang/Object;Lorg/ovirt/engine/core/compat/EventArgs;)(model, errorCodeEventArgs);
            
            // Refresh grid on disconnect (to re-enable console button)
            var gridRefreshManager = @org.ovirt.engine.ui.userportal.client.components.GridRefreshManager::getInstance()();           
            gridRefreshManager.@org.ovirt.engine.ui.userportal.client.components.GridRefreshManager::refreshGrids()();            
        }
	}-*/;

	@Override
	public void Install() {
		logger.finer("Spice must be installed ahead...");
		InstallNatively();
	}

	public native void InstallNatively() /*-{
		alert("Spice must be already installed!");
	}-*/;

	@Override
	public boolean getIsInstalled() {
		boolean result = getIsInstalledNative();
		logger.finer("Determining whether spice is installed [" + result + "]");

		return result;
	}

	public native boolean getIsInstalledNative() /*-{
		return true;
		//var daPlugins = "Spice";
		//var pluginFound = false;
		//if (navigator.plugins && navigator.plugins.length > 0) {
		//var pluginsArrayLength = navigator.plugins.length;
		//for (pluginsArrayCounter=0; pluginsArrayCounter < pluginsArrayLength; pluginsArrayCounter++ ) {
		//var numFound = 0;
		//for(namesCounter=0; namesCounter < daPlugins.length; namesCounter++) {
		//if( (navigator.plugins[pluginsArrayCounter].name.indexOf(daPlugins[namesCounter]) >= 0) || 
		//(navigator.plugins[pluginsArrayCounter].description.indexOf(daPlugins[namesCounter]) >= 0) ) {
		//numFound++;
		//}
		//}
		//if(numFound == daPlugins.length) {
		//pluginFound = true;
		//break;
		//}
		//}
		//}
		//return pluginFound;
	}-*/;

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
