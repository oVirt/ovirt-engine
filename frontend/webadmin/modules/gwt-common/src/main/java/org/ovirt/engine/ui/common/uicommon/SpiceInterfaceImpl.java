package org.ovirt.engine.ui.common.uicommon;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpice;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.WANDisableEffects;
import org.ovirt.engine.ui.uicommonweb.models.vms.WanColorDepth;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public class SpiceInterfaceImpl implements ISpice {
    private static Logger logger = Logger.getLogger(SpiceInterfaceImpl.class
            .getName());

    private Event disconnectedEvent = new Event(
            SpiceConsoleModel.SpiceDisconnectedEventDefinition);
    private Event connectedEvent = new Event(
            SpiceConsoleModel.SpiceConnectedEventDefinition);
    private Event menuItemSelectedEvent = new Event(
            SpiceConsoleModel.SpiceMenuItemSelectedEventDefinition);
    private Event usbAutoShareChangedEvent = new Event(
            SpiceConsoleModel.UsbAutoShareChangedEventDefinition);

    private final Event wanColorDepthChangedEvent = new Event(
            SpiceConsoleModel.wanColorDepthChangedEventDefinition);

    private final Event wanDisableEffectsChangeEvent = new Event(
            SpiceConsoleModel.wanDisableEffectsChangeEventDefinition);

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
    private WanColorDepth wanColorDepth;
    private List<WANDisableEffects> wanDisableEffects;
    private boolean wanOptionsEnabled;
    ClientAgentType cat = new ClientAgentType();
    private String spiceBaseURL;
    private boolean smartcardEnabled = false;

    // the user can choose to disable the smartcard even when it is enabled, but can not choose to enable it, when it is disabled
    private boolean smartcardEnabledOverridden = false;

    public SpiceInterfaceImpl() {
        logger.fine("Instantiating GWT Spice Implementation"); //$NON-NLS-1$
        wanDisableEffects = new ArrayList<WANDisableEffects>();
        wanOptionsEnabled = false;
        wanColorDepth = WanColorDepth.depth16;

        // send the ctrl + alt + delete by default
        sendCtrlAltDelete = true;
        noTaskMgrExecution = true;
    }

    @Override
    public void setWANDisableEffects(List<WANDisableEffects> disableEffects) {
        this.wanDisableEffects = disableEffects;
        getWANDisableEffectsChangeEvent().raise(this, EventArgs.Empty);
    }

    @Override
    public void setWANColorDepth(WanColorDepth colorDepth) {
        this.wanColorDepth = colorDepth;
        getWANColorDepthChangedEvent().raise(this, EventArgs.Empty);
    }

    @Override
    public List<WANDisableEffects> getWANDisableEffects() {
        return wanDisableEffects;
    }

    @Override
    public WanColorDepth getWANColorDepth() {
        return wanColorDepth;
    }

    @Override
    public Event getWANDisableEffectsChangeEvent() {
        return wanDisableEffectsChangeEvent;
    }

    @Override
    public Event getWANColorDepthChangedEvent() {
        return wanColorDepthChangedEvent;
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

    public void setUsbAutoShareChangedEvent(Event usbAutoShareChangedEvent) {
        this.usbAutoShareChangedEvent = usbAutoShareChangedEvent;
    }

    @Override
    public Event getUsbAutoShareChangedEvent() {
        return usbAutoShareChangedEvent;
    }

    @Override
    public Version getCurrentVersion() {
        return currentVersion;
    }

    @Override
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
        getUsbAutoShareChangedEvent().raise(this, EventArgs.Empty);
    }

    @Override
    public String getUsbFilter() {
        return usbFilter;
    }

    @Override
    public void setUsbFilter(String usbFilter) {
        this.usbFilter = usbFilter;
    }

    public String getSpiceBaseURL() {
        return spiceBaseURL;
    }

    @Override
    public void setSpiceBaseURL(String spiceBaseURL) {
        this.spiceBaseURL = spiceBaseURL;
    }

    public boolean passSmartcardOption() {
        return isSmartcardEnabled() && !isSmartcardEnabledOverridden();
    }

    @Override
    public boolean isSmartcardEnabled() {
        return smartcardEnabled;
    }

    @Override
    public void setSmartcardEnabled(boolean smartcardEnabled) {
        this.smartcardEnabled = smartcardEnabled;
    }

    private int colorDepthAsInt() {
        if (getWANColorDepth() != null) {
            return getWANColorDepth().asInt();
        }

        return WanColorDepth.depth16.asInt();
    }

    private String disbaleEffectsAsString() {
        StringBuffer disableEffectsBuffer = new StringBuffer("");
        int countdown = getWANDisableEffects().size();
        for (WANDisableEffects disabledEffect : getWANDisableEffects()) {
            disableEffectsBuffer.append(disabledEffect.asString());

            if (countdown != 1) {
                disableEffectsBuffer.append(", "); //$NON-NLS-1$
            }
            countdown--;
        }

        return disableEffectsBuffer.toString();
    }

    @Override
    public void Connect() {
        logger.warning("Connecting via spice..."); //$NON-NLS-1$

        if ((cat.os.equalsIgnoreCase("Linux")) //$NON-NLS-1$
                && (cat.browser.equalsIgnoreCase("Firefox"))) { //$NON-NLS-1$
            connectNativelyViaXPI();
        } else if ((cat.os.equalsIgnoreCase("Windows")) //$NON-NLS-1$
                && (cat.browser.equalsIgnoreCase("Explorer"))) { //$NON-NLS-1$
            connectNativelyViaActiveX();
        }
    }

    public String getSpiceCabURL() {
        // According to the OS type, return the appropriate CAB URL
        if (cat.getPlatform().equalsIgnoreCase("win32")) { //$NON-NLS-1$
            return spiceBaseURL + "SpiceX.cab"; //$NON-NLS-1$
        } else if (cat.getPlatform().equalsIgnoreCase("win64")) { //$NON-NLS-1$
            return spiceBaseURL + "SpiceX_x64.cab"; //$NON-NLS-1$
        } else {
            return null;
        }
    }

    public String getSpiceObjectClassId() {
        // According to the OS type, return the appropriate (x64/x86) object
        // class ID
        if (cat.getPlatform().equalsIgnoreCase("win32")) { //$NON-NLS-1$
            return "ACD6D89C-938D-49B4-8E81-DDBD13F4B48A"; //$NON-NLS-1$
        } else if (cat.getPlatform().equalsIgnoreCase("win64")) { //$NON-NLS-1$
            return "ACD6D89C-938D-49B4-8E81-DDBD13F4B48B"; //$NON-NLS-1$
        } else {
            return null;
        }
    }

    public native String loadActiveX(String id, String codebase, String classId) /*-{
                                                                                 var container = $wnd.document.createElement("div");
                                                                                 container.innerHTML = '<object id="' + id + '" codebase="' + codebase + '" classid="CLSID:' + classId
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
                                            var container = document.createElement("div");
                                            container.innerHTML = '<embed id="' + id + '" type="application/x-spice" width=0 height=0/>';
                                            container.style.width = "0px";
                                            container.style.height = "0px";
                                            container.style.position = "absolute";
                                            container.style.top = "0px";
                                            container.style.left = "0px";
                                            var target_element = document.getElementsByTagName("body")[0];
                                            if (typeof (target_element) == "undefined" || target_element == null)
                                            return false;
                                            target_element.appendChild(container);
                                            }-*/;

    public native void connectNativelyViaXPI() /*-{
                                               var pluginFound = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::detectXpiPlugin()();
                                               if (!pluginFound) {
                                               alert("Spice XPI addon was not found, please install Spice XPI addon first.");
                                               return;
                                               }

                                               var hostIp = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getHost()();
                                               var port = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getPort()();
                                               var fullScreen = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getFullScreen()();
                                               var password = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getPassword()();
                                               var numberOfMonitors = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getNumberOfMonitors()();
                                               var usbListenPort = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getUsbListenPort()();
                                               var adminConsole = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getAdminConsole()();
                                               var guestHostName = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getGuestHostName()();
                                               var securePort = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getSecurePort()();
                                               var sslChanels = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getSslChanels()();
                                               var cipherSuite = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getCipherSuite()();
                                               var hostSubject = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getHostSubject()();
                                               var trustStore = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getTrustStore()();
                                               var title = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getTitle()();
                                               var hotKey = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getHotKey()();
                                               var menu = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getMenu()();
                                               var guestID = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getGuestID()();
                                               var version = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getCurrentVersion()();
                                               var spiceCabURL = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getSpiceCabURL()();
                                               var spiceCabOjectClassId = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getSpiceObjectClassId()();
                                               var id = "SpiceX_" + guestHostName;
                                               var noTaskMgrExecution = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getNoTaskMgrExecution()();
                                               var sendCtrlAltDelete = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getSendCtrlAltDelete()();
                                               var usbAutoShare = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getUsbAutoShare()();
                                               var usbFilter = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getUsbFilter()();
                                               var disconnectedEvent = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getDisconnectedEvent()();
                                               var connectedEvent = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getConnectedEvent()();
                                               var wanOptionsEnabled = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getIsWanOptionsEnabled()();
                                               // the !! is there to convert the value to boolean because it is returned as int
                                               var smartcardEnabled =  !!this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::passSmartcardOption()();
                                               var colorDepth = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::colorDepthAsInt()();
                                               var disableEffects = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::disbaleEffectsAsString()();
                                               var model = this;

                                               //alert("Smartcard ["+smartcardEnabled+"] disableEffects ["+disableEffects+"], wanOptionsEnabled ["+wanOptionsEnabled+"], colorDepth ["+colorDepth+"], Host IP ["+hostIp+"], port ["+port+"], fullScreen ["+fullScreen+"], password ["+password+"], numberOfMonitors ["+numberOfMonitors+"], Usb Listen Port ["+usbListenPort+"], Admin Console ["+adminConsole+"], Guest HostName ["+guestHostName+"], Secure Port ["+securePort+"], Ssl Chanels ["+sslChanels+"], cipherSuite ["+cipherSuite+"], Host Subject ["+hostSubject+"], Title [" + title+"], Hot Key ["+hotKey+"], Menu ["+menu+"], GuestID [" + guestID+"], version ["+version+"]");
                                               this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::loadXpi(Ljava/lang/String;)(id);
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
                                               client.Smartcard = smartcardEnabled;
                                               if (wanOptionsEnabled) {
                                                  client.DisableEffects = disableEffects;
                                                  client.ColorDepth = colorDepth;
                                               }
                                               client.connect();

                                               connectedEvent.@org.ovirt.engine.ui.uicompat.Event::raise(Ljava/lang/Object;Lorg/ovirt/engine/ui/uicompat/EventArgs;)(model, null);

                                               //since the 'ondisconnected' event doesn't work well in linux, we use polling instead:
                                               var checkConnectStatusIntervalID = setInterval(checkConnectStatus, 2000);
                                               function checkConnectStatus() {
                                               if (client.ConnectedStatus() >= 0) {
                                               clearInterval(checkConnectStatusIntervalID);

                                               var errorCodeEventArgs = @org.ovirt.engine.ui.uicommonweb.models.vms.ErrorCodeEventArgs::new(I)(client.ConnectedStatus());
                                               disconnectedEvent.@org.ovirt.engine.ui.uicompat.Event::raise(Ljava/lang/Object;Lorg/ovirt/engine/ui/uicompat/EventArgs;)(model, errorCodeEventArgs);

                                               // Refresh grid on disconnect (to re-enable console button)
                                               // var gridRefreshManager = @org.ovirt.engine.ui.userportal.client.components.GridRefreshManager::getInstance()();
                                               // gridRefreshManager.@org.ovirt.engine.ui.userportal.client.components.GridRefreshManager::refreshGrids()();
                                               }
                                               }
                                               }-*/;

    public native boolean detectXpiPlugin() /*-{
                                            var pluginsFound = false;
                                            if (navigator.plugins && navigator.plugins.length > 0) {
                                            var daPlugins = [ "Spice" ];
                                            var pluginsAmount = navigator.plugins.length;
                                            for (counter = 0; counter < pluginsAmount; counter++) {
                                            var numFound = 0;
                                            for (namesCounter = 0; namesCounter < daPlugins.length; namesCounter++) {
                                            if ((navigator.plugins[counter].name.indexOf(daPlugins[namesCounter]) > 0)
                                            || (navigator.plugins[counter].description.indexOf(daPlugins[namesCounter]) >= 0)) {
                                            numFound++;
                                            }
                                            }
                                            if (numFound == daPlugins.length) {
                                            pluginsFound = true;
                                            break;
                                            }
                                            }

                                            }
                                            return pluginsFound;
                                            }-*/;

    public native void connectNativelyViaActiveX() /*-{
                                                   var hostIp = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getHost()();
                                                   var port = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getPort()();
                                                   var fullScreen = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getFullScreen()();
                                                   var password = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getPassword()();
                                                   var numberOfMonitors = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getNumberOfMonitors()();
                                                   var usbListenPort = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getUsbListenPort()();
                                                   var adminConsole = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getAdminConsole()();
                                                   var guestHostName = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getGuestHostName()();
                                                   var securePort = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getSecurePort()();
                                                   var sslChanels = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getSslChanels()();
                                                   var cipherSuite = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getCipherSuite()();
                                                   var hostSubject = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getHostSubject()();
                                                   var trustStore = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getTrustStore()();
                                                   var title = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getTitle()();
                                                   var hotKey = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getHotKey()();
                                                   var menu = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getMenu()();
                                                   var guestID = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getGuestID()();
                                                   var version = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getDesiredVersionStr()();
                                                   var spiceCabURL = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getSpiceCabURL()();
                                                   var spiceCabOjectClassId = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getSpiceObjectClassId()();
                                                   var noTaskMgrExecution = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getNoTaskMgrExecution()();
                                                   var sendCtrlAltDelete = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getSendCtrlAltDelete()();
                                                   var usbAutoShare = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getUsbAutoShare()();
                                                   var usbFilter = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getUsbFilter()();
                                                   var disconnectedEvent = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getDisconnectedEvent()();
                                                   var connectedEvent = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getConnectedEvent()();
                                                   var wanOptionsEnabled = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::getIsWanOptionsEnabled()();
                                                   var colorDepth = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::colorDepthAsInt()();
                                                   var disableEffects = this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::disbaleEffectsAsString()();
                                                   var codebase = spiceCabURL + "#version=" + version;
                                                   var model = this;
                                                   var id = "SpiceX_" + guestHostName;
                                                   //alert("Host IP ["+hostIp+"], port ["+port+"], fullScreen ["+fullScreen+"], password ["+password+"], numberOfMonitors ["+numberOfMonitors+"], Usb Listen Port ["+usbListenPort+"], Admin Console ["+adminConsole+"], Guest HostName ["+guestHostName+"], Secure Port ["+securePort+"], Ssl Chanels ["+sslChanels+"], cipherSuite ["+cipherSuite+"], Host Subject ["+hostSubject+"], Title [" + title+"], Hot Key ["+hotKey+"], Menu ["+menu+"], GuestID [" + guestID+"], version ["+version+"]");
                                                   //alert("Trust Store ["+trustStore+"]");

                                                   this.@org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl::loadActiveX(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(id,codebase,spiceCabOjectClassId);

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
                                                   client.SetUsbFilter(usbFilter);
                                                   if (wanOptionsEnabled) {
                                                       client.DisableEffects = disableEffects;
                                                       client.ColorDepth = colorDepth;
                                                   }

                                                   client.attachEvent('ondisconnected', onDisconnected);
                                                   client.connect();

                                                   connectedEvent.@org.ovirt.engine.ui.uicompat.Event::raise(Ljava/lang/Object;Lorg/ovirt/engine/ui/uicompat/EventArgs;)(model, null);
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
                                                   disconnectedEvent.@org.ovirt.engine.ui.uicompat.Event::raise(Ljava/lang/Object;Lorg/ovirt/engine/ui/uicompat/EventArgs;)(model, errorCodeEventArgs);

                                                   // Refresh grid on disconnect (to re-enable console button)
                                                   // var gridRefreshManager = @org.ovirt.engine.ui.userportal.client.components.GridRefreshManager::getInstance()();
                                                   // gridRefreshManager.@org.ovirt.engine.ui.userportal.client.components.GridRefreshManager::refreshGrids()();
                                                   }
                                                   }-*/;

    @Override
    public void Install() {
        logger.finer("Spice must be installed ahead..."); //$NON-NLS-1$
        InstallNatively();
    }

    public native void InstallNatively() /*-{
                                         alert("Spice must be already installed!");
                                         }-*/;

    @Override
    public boolean getIsInstalled() {
        boolean result = getIsInstalledNative();
        logger.finer("Determining whether spice is installed [" + result + "]"); //$NON-NLS-1$ //$NON-NLS-2$

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
        logger.finer("Determining whether browser [" + cat.browser //$NON-NLS-1$
                + "], version [" + cat.version + "] on OS [" + cat.os //$NON-NLS-1$ //$NON-NLS-2$
                + "] is supported by spice"); //$NON-NLS-1$

        if ((cat.os.equalsIgnoreCase("Windows")) //$NON-NLS-1$
                && (cat.browser.equalsIgnoreCase("Explorer")) //$NON-NLS-1$
                && (cat.version >= 7.0)) {
            return true;
        } else if ((cat.os.equalsIgnoreCase("Linux")) //$NON-NLS-1$
                && (cat.browser.equalsIgnoreCase("Firefox")) //$NON-NLS-1$
                && (cat.version >= 2.0)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean getIsWanOptionsEnabled() {
        return wanOptionsEnabled;
    }

    @Override
    public void setIsWanOptionsEnabled(boolean enabled) {
        this.wanOptionsEnabled = enabled;
    }

    @Override
    public void setOverrideEnabledSmartcard(boolean enabled) {
        this.smartcardEnabledOverridden = enabled;
    }

    /**
     * Returns true if the user has choosen to disable the smartcard even it is by default enabled
     */
    @Override
    public boolean isSmartcardEnabledOverridden() {
        return this.smartcardEnabledOverridden;
    }

}
